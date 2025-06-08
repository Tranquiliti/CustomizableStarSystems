package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl.createInitialPeople;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;
import static org.tranquility.customizablestarsystems.CSSStrings.*;

/**
 * A custom star system
 */
@SuppressWarnings({"unchecked", "FieldCanBeLocal"})
public class CustomStarSystem {
    // Note: Starsector mainly uses floats, but JSON method only supports doubles when extracting floating-point numbers
    // Hence, most options only take in integers - mainly so fewer casts are needed

    // Default values
    public static final int DEFAULT_NUMBER_OF_SYSTEMS = 1;
    private final int DEFAULT_SET_TO_PROC_GEN = -1; // For user's sake, referenceStarSystem.json uses 0 to specify proc-gen
    private final int DEFAULT_MARKET_SIZE = 0;

    // These variables are not static since this code usually runs once and then never again during gameplay
    // They are also not local to make tracking down Strings easier for externalization purposes
    // Entities, usually used in switch cases
    private final String ENTITY_EMPTY_LOCATION = "empty_location";
    private final String ENTITY_REMNANT_STATION = "remnant_station";
    private final String ENTITY_RINGS_ICE = "rings_ice0";
    private final String ENTITY_RINGS_DUST = "rings_dust0";
    private final String ENTITY_RINGS_SPECIAL = "rings_special0";
    private final String ENTITY_RINGS_ASTEROIDS = "rings_asteroids0";

    // Dev/internal ids
    private final String ID_SYSTEM = "system_"; // Most system entity ids should start with this
    private final String ID_STAR = ":star_";
    private final String ID_PLANET = ":planet_";
    private final String ID_STATION = ":station_";
    private final String ID_MARKET = "market_";
    private final String CONDITION_POPULATION = "population_"; // addMarket() appends a number to this

    // addRemnantStation() strings
    private final String ID_REMNANT_STATION_DAMAGED = "remnant_station2_Damaged";
    private final String ID_REMNANT_STATION_STANDARD = "remnant_station2_Standard";
    private final String MEMFLAGS_DAMAGED_STATION = "$damagedStation";

    // Other
    private final Random randomSeed = StarSystemGenerator.random; // Sector seed
    private final List<Constellation> constellations; // Used in setConstellationLocation()
    private final Map<MarketAPI, String> marketsToOverrideAdmin; // Updated in addMarket()
    private final boolean initPeople;

    private final String SYSTEM_ID;
    private final JSONObject SYSTEM_OPTIONS;
    private StarSystemAPI system;
    private List<SectorEntityToken> systemEntities;
    private float systemRadius = 0f;
    private boolean hasFactionPresence = false;
    private boolean hasJumpPoint = false;

    /**
     * Creates a custom star system from JSON
     *
     * @param systemOptions          System options
     * @param systemId               ID of the custom star system options
     * @param constellations         List of constellations in which to place the system
     * @param marketsToOverrideAdmin Map of markets that need admin replacements
     * @throws JSONException If systemOptions is invalid
     */
    public CustomStarSystem(JSONObject systemOptions, String systemId, List<Constellation> constellations, Map<MarketAPI, String> marketsToOverrideAdmin, boolean initPeople) throws JSONException {
        SYSTEM_ID = systemId;
        SYSTEM_OPTIONS = systemOptions;
        this.constellations = constellations;
        this.marketsToOverrideAdmin = marketsToOverrideAdmin;
        this.initPeople = initPeople;

        createStarSystem();
        generateEntities();

        generateDomainCryosleeperIfApplicable();
        generateCoronalHypershuntIfApplicable();
        addMusicIfApplicable();

        setLocation();

        addNebulaIfApplicable();
        setBackground();
        addSystemTagsIfApplicable();
        setLightColor();
        generateHyperspace();
        addRemnantWarningBeaconsIfApplicable();
    }

    /**
     * Gets the actual star system
     *
     * @return The StarSystemAPI represented by this CustomStarSystem
     */
    public StarSystemAPI getSystem() {
        return system;
    }

    private void createStarSystem() throws JSONException {
        JSONArray entities = SYSTEM_OPTIONS.getJSONArray(OPT_ENTITIES);

        // Create a star system based on the 1st star's name
        // Looping through "entities" list since 1st entity may be an "empty_location" and not a star
        for (int i = 0; system == null && i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            if (entity.getString(OPT_ENTITY).equals(Tags.STAR))
                system = Global.getSector().createStarSystem(entity.isNull(OPT_NAME) ? getProcGenName(Tags.STAR, null) : entity.getString(OPT_NAME));
        }
    }

    private void generateEntities() throws JSONException {
        JSONArray entities = SYSTEM_OPTIONS.getJSONArray(OPT_ENTITIES);
        if (entities.length() == 0) throw new IllegalArgumentException(String.format(ERROR_BAD_CENTER_STAR, SYSTEM_ID));

        systemEntities = new ArrayList<>(entities.length());

        for (int i = 0; i < entities.length(); i++) {
            JSONObject entityOptions = entities.getJSONObject(i);
            String entityType = entityOptions.getString(OPT_ENTITY);

            if (i == 0 && !entityType.equals(Tags.STAR) && !entityType.equals(ENTITY_EMPTY_LOCATION))
                throw new IllegalArgumentException(String.format(ERROR_BAD_CENTER_STAR, SYSTEM_ID));

            SectorEntityToken newEntity;
            switch (entityType) {
                case ENTITY_EMPTY_LOCATION:
                    if (i == 0) { // Make a non-star center
                        i += addCenterStars(entities);
                        continue;
                    } else {
                        newEntity = system.createToken(0f, 0f);
                        system.addEntity(newEntity);
                        setEntityLocation(newEntity, entityOptions, i);
                    }
                    break;
                case Tags.STAR:
                    newEntity = addStar(entityOptions, i, false);
                    if (i != 0) {
                        setEntityLocation(newEntity, entityOptions, i);
                        updateSystemRadius(newEntity);
                    }
                    break;
                case Tags.PLANET:
                    newEntity = addPlanet(entityOptions, i);
                    setEntityLocation(newEntity, entityOptions, i);
                    updateSystemRadius(newEntity);
                    addSolarArrayIfApplicable((PlanetAPI) newEntity);
                    break;
                case Tags.JUMP_POINT:
                    newEntity = addJumpPoint(entityOptions);
                    setEntityLocation(newEntity, entityOptions, i);
                    updateSystemRadius(newEntity);
                    break;
                case Tags.STATION:
                    newEntity = addStation(entityOptions, i);
                    setEntityLocation(newEntity, entityOptions, i);
                    break;
                case ENTITY_REMNANT_STATION:
                    newEntity = addRemnantStation(entityOptions);
                    setEntityLocation(newEntity, entityOptions, i);
                    break;
                case Terrain.ASTEROID_FIELD:
                    newEntity = addAsteroidField(entityOptions);
                    setEntityLocation(newEntity, entityOptions, i);
                    break;
                case Terrain.DEBRIS_FIELD:
                    newEntity = addDebrisField(entityOptions);
                    setEntityLocation(newEntity, entityOptions, i);
                    break;
                case Tags.ACCRETION_DISK:
                    newEntity = addAccretionDisk(entityOptions, i);
                    break;
                case Terrain.MAGNETIC_FIELD:
                    newEntity = addMagneticField(entityOptions, i);
                    break;
                case Terrain.RING:
                    newEntity = addRingBand(entityOptions, i);
                    break;
                case Terrain.ASTEROID_BELT:
                    newEntity = addAsteroidBelt(entityOptions, i);
                    break;
                case Entities.WRECK:
                    newEntity = addDerelictShip(entityOptions, i);
                    break;
                default:
                    newEntity = addCustomEntity(entityOptions);
                    setEntityLocation(newEntity, entityOptions, i);
            }

            addMemoryKeys(newEntity, entityOptions, OPT_MEMORY_KEYS);
            addTags(newEntity, entityOptions);

            systemEntities.add(newEntity);
        }

        if (hasFactionPresence) {
            system.setEnteredByPlayer(true);
            Misc.setAllPlanetsSurveyed(system, true);
        }

        // Fallback option for systems with no orbiting bodies or jump-points
        if (systemRadius == 0f) systemRadius = system.getStar().getRadius() + 1000f;
    }

    private void updateSystemRadius(SectorEntityToken entity) {
        if (entity.getCircularOrbitRadius() > systemRadius) systemRadius = entity.getCircularOrbitRadius();
    }

    private void setEntityLocation(SectorEntityToken entity, JSONObject entityOptions, int index) throws JSONException {
        if (entityOptions.optJSONArray(OPT_FOCUS) == null) setCircularOrbit(entity, entityOptions, index);
        else setLagrangePointOrbit(entity, entityOptions);
    }

    private void setCircularOrbit(SectorEntityToken entity, JSONObject entityOptions, int index) throws JSONException {
        SectorEntityToken focusEntity = getFocusEntity(entityOptions, index);

        String type = entityOptions.getString(OPT_ENTITY);

        float orbitRadius = entityOptions.getInt(OPT_ORBIT_RADIUS);

        float angle = entityOptions.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
        if (angle < 0) angle = randomSeed.nextFloat() * 360f;

        float orbitDays = entityOptions.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0) {
            float divisor = switch (type) {
                case Entities.INACTIVE_GATE -> 10f;
                case Tags.JUMP_POINT -> 15f;
                default -> 20f;
            };
            orbitDays = orbitRadius / (divisor + randomSeed.nextFloat() * 5f);
        }

        if (!entityOptions.optBoolean(OPT_ORBIT_CLOCKWISE, true)) orbitDays *= -1f;

        // Could probably clean this up later
        switch (type) {
            case ENTITY_REMNANT_STATION:
                entity.setCircularOrbitWithSpin(focusEntity, angle, orbitRadius, orbitDays, 5f, 5f);
                break;
            case Entities.STABLE_LOCATION, Entities.COMM_RELAY, Entities.COMM_RELAY_MAKESHIFT, Entities.NAV_BUOY,
                 Entities.NAV_BUOY_MAKESHIFT, Entities.SENSOR_ARRAY, Entities.SENSOR_ARRAY_MAKESHIFT, Tags.STATION,
                 Entities.CORONAL_TAP:
                entity.setCircularOrbitPointingDown(focusEntity, angle, orbitRadius, orbitDays);
                break;
            default:
                entity.setCircularOrbit(focusEntity, angle, orbitRadius, orbitDays);
        }
    }

    private void setLagrangePointOrbit(SectorEntityToken entity, JSONObject entityOptions) throws JSONException {
        SectorEntityToken focusEntity = systemEntities.get(entityOptions.getJSONArray(OPT_FOCUS).getInt(0));

        float orbitAngle = focusEntity.getCircularOrbitAngle();
        float orbitRadius = focusEntity.getCircularOrbitRadius();

        int lagrangePoint = entityOptions.getJSONArray(OPT_FOCUS).getInt(1);
        switch (lagrangePoint) {
            case 1:
                orbitRadius -= focusEntity.getRadius() - 200f;
                break;
            case 2:
                orbitRadius += focusEntity.getRadius() + 200f;
                break;
            case 3:
                orbitAngle -= 180f;
                break;
            case 4:
                orbitAngle += 60f;
                break;
            case 5:
                orbitAngle -= 60f;
                break;
        }

        if (entityOptions.getString(OPT_ENTITY).equals(Tags.PLANET))
            entity.setCircularOrbit(focusEntity.getOrbitFocus(), orbitAngle, orbitRadius, focusEntity.getCircularOrbitPeriod());
        else
            entity.setCircularOrbitPointingDown(focusEntity.getOrbitFocus(), orbitAngle, orbitRadius, focusEntity.getCircularOrbitPeriod());
    }

    // Special case for handling multiple stars orbiting a non-star center
    private int addCenterStars(JSONArray entities) throws JSONException {
        JSONObject entityOptions = entities.getJSONObject(0);
        systemEntities.add(system.initNonStarCenter());
        systemEntities.get(0).setId(ID_SYSTEM + systemEntities.get(0).getId());

        int numOfCenterStars = entityOptions.getInt(OPT_NUM_OF_CENTER_STARS);

        float orbitRadius = entityOptions.optInt(OPT_ORBIT_RADIUS, 2000);

        float angle = entityOptions.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
        if (angle < 0) angle = randomSeed.nextFloat() * 360f;

        float orbitDays = entityOptions.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0) orbitDays = orbitRadius / (20f + randomSeed.nextFloat() * 5f);

        float angleDifference = 360f / numOfCenterStars;
        for (int i = 1; i <= numOfCenterStars; i++) {
            JSONObject starOptions = entities.getJSONObject(i);
            if (!starOptions.getString(OPT_ENTITY).equals(Tags.STAR))
                throw new IllegalArgumentException(String.format(ERROR_BAD_CENTER_STAR, SYSTEM_ID));
            systemEntities.add(addStar(starOptions, i, true));
            systemEntities.get(i).setCircularOrbit(system.getCenter(), angle, orbitRadius + i - 1, orbitDays);
            angle = (angle + angleDifference) % 360f;
        }

        return systemEntities.size() - 1;
    }

    private PlanetAPI addStar(JSONObject options, int index, boolean isClose) throws JSONException {
        JSONArray typeList = options.optJSONArray(OPT_TYPE);
        String starType;
        if (typeList != null) starType = typeList.getString(randomSeed.nextInt(typeList.length()));
        else starType = options.optString(OPT_TYPE, DEFAULT_STAR_TYPE);

        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, starType, true);
        if (starData == null)
            throw new IllegalArgumentException(String.format(ERROR_STAR_TYPE_NOT_FOUND, starType, SYSTEM_ID, index));

        float radius = options.optInt(OPT_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (radius <= 0)
            radius = starData.getMinRadius() + (starData.getMaxRadius() - starData.getMinRadius()) * randomSeed.nextFloat();

        float coronaRadius = options.optInt(OPT_CORONA_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (coronaRadius <= 0)
            coronaRadius = Math.max(starData.getCoronaMin(), radius * (starData.getCoronaMult() + starData.getCoronaVar() * (randomSeed.nextFloat() - 0.5f)));

        float flareChance = (float) options.optDouble(OPT_FLARE_CHANCE, DEFAULT_SET_TO_PROC_GEN);
        if (flareChance < 0)
            flareChance = starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * randomSeed.nextFloat();

        PlanetAPI newStar;
        if (system.getCenter() == null) {
            newStar = system.initStar(null, starType, radius, coronaRadius, starData.getSolarWind(), flareChance, starData.getCrLossMult());
            newStar.setId(ID_SYSTEM + newStar.getId());
        } else {
            String name = options.optString(OPT_NAME, null);
            if (name == null) name = getProcGenName(Tags.STAR, system.getBaseName());

            // Need to set a default orbit, else new game creation will fail when attempting to save
            newStar = system.addPlanet(system.getCenter().getId() + ID_STAR + index, system.getCenter(), name, starType, 0f, radius, 10000f, 1000f);
            system.addCorona(newStar, coronaRadius, starData.getSolarWind(), flareChance, starData.getCrLossMult());

            switch (system.getType()) {
                case SINGLE:
                    if (system.getStar() == null) {
                        system.setStar(newStar);
                    } else {
                        system.setSecondary(newStar);
                        system.setType(isClose ? StarSystemGenerator.StarSystemType.BINARY_CLOSE : StarSystemGenerator.StarSystemType.BINARY_FAR);
                    }
                    break;
                case BINARY_CLOSE:
                    system.setTertiary(newStar);
                    system.setType(isClose ? StarSystemGenerator.StarSystemType.TRINARY_2CLOSE : StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR);
                    break;
                case BINARY_FAR:
                    system.setTertiary(newStar);
                    system.setType(isClose ? StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR : StarSystemGenerator.StarSystemType.TRINARY_2FAR);
                    break;
            }
        }

        if (starType.equals(StarTypes.BLACK_HOLE) || starType.equals(StarTypes.NEUTRON_STAR)) {
            StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(newStar);
            if (coronaPlugin != null) system.removeEntity(coronaPlugin.getEntity());

            String coronaType = starType.equals(StarTypes.BLACK_HOLE) ? Terrain.EVENT_HORIZON : Terrain.PULSAR_BEAM;
            if (coronaType.equals(Terrain.PULSAR_BEAM)) system.addCorona(newStar, 300, 3, 0, 3);
            system.addTerrain(coronaType, new StarCoronaTerrainPlugin.CoronaParams(newStar.getRadius() + coronaRadius, (newStar.getRadius() + coronaRadius) / 2f, newStar, starData.getSolarWind(), flareChance, starData.getCrLossMult())).setCircularOrbit(newStar, 0, 0, 100);
        }

        addSpecChanges(newStar, options.optJSONObject(OPT_SPEC_CHANGES));

        addCustomDescription(newStar, options);

        return newStar;
    }

    private PlanetAPI addPlanet(JSONObject options, int index) throws JSONException {
        JSONArray typeList = options.optJSONArray(OPT_TYPE);
        String planetType;
        if (typeList != null) planetType = typeList.getString(randomSeed.nextInt(typeList.length()));
        else planetType = options.optString(OPT_TYPE, DEFAULT_PLANET_TYPE);

        PlanetGenDataSpec planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planetType, true);
        if (planetData == null)
            throw new IllegalArgumentException(String.format(ERROR_PLANET_TYPE_NOT_FOUND, planetType, SYSTEM_ID, index));

        String name = options.optString(OPT_NAME, null);
        if (name == null) name = getProcGenName(Tags.PLANET, system.getBaseName());

        float radius = options.optInt(OPT_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (radius <= 0)
            radius = planetData.getMinRadius() + (planetData.getMaxRadius() - planetData.getMinRadius()) * randomSeed.nextFloat();

        // Need to set a default orbit, else new game creation will fail when attempting to save
        PlanetAPI newPlanet = system.addPlanet(system.getCenter().getId() + ID_PLANET + index, system.getCenter(), name, planetType, 0f, radius, 10000f, 1000f);

        addSpecChanges(newPlanet, options.optJSONObject(OPT_SPEC_CHANGES));

        addCustomDescription(newPlanet, options);

        int marketSize = options.optInt(OPT_MARKET_SIZE, DEFAULT_MARKET_SIZE);
        if (marketSize <= 0) setPlanetConditions(newPlanet, options); // initConditionMarket() sets salvage seed already
        else {
            newPlanet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, randomSeed.nextLong());
            addMarket(newPlanet, options, marketSize);
        }

        return newPlanet;
    }

    private JumpPointAPI addJumpPoint(JSONObject options) {
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(null, options.optString(OPT_NAME, null));
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        hasJumpPoint = true;

        return jumpPoint;
    }

    private SectorEntityToken addStation(JSONObject options, int index) throws JSONException {
        SectorEntityToken station = system.addCustomEntity(system.getStar().getId() + ID_STATION + index, options.optString(OPT_NAME, null), options.optString(OPT_TYPE, DEFAULT_STATION_TYPE), options.optString(OPT_FACTION_ID, null));
        addCustomDescription(station, options);

        int marketSize = options.optInt(OPT_MARKET_SIZE, DEFAULT_MARKET_SIZE);
        if (marketSize <= 0) setStationConditions(station, options);
        else addMarket(station, options, marketSize);

        return station;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator's addBattlestations() for vanilla implementation
    private CampaignFleetAPI addRemnantStation(JSONObject options) {
        boolean isDamaged = options.optBoolean(OPT_IS_DAMAGED, false);

        CampaignFleetAPI station = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, isDamaged ? ID_REMNANT_STATION_DAMAGED : ID_REMNANT_STATION_STANDARD);
        station.getFleetData().addFleetMember(member);

        station.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        station.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
        station.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
        station.addTag(Tags.NEUTRINO_HIGH);

        station.setStationMode(true);
        RemnantThemeGenerator.addRemnantStationInteractionConfig(station);
        system.addEntity(station);

        station.clearAbilities();
        station.addAbility(Abilities.TRANSPONDER);
        station.getAbility(Abilities.TRANSPONDER).activate();
        station.getDetectedRangeMod().modifyFlat(SOURCE_GEN, 1000f);

        station.setAI(null);

        PersonAPI commander = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE).createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, randomSeed);

        station.setCommander(commander);
        station.getFlagship().setCaptain(commander);

        if (isDamaged) {
            station.getMemoryWithoutUpdate().set(MEMFLAGS_DAMAGED_STATION, true);
            station.setName(station.getName() + NAME_DAMAGED_STATION);

            system.addScript(new RemnantStationFleetManager(station, 1f, 0, 2 + randomSeed.nextInt(3), 25f, 6, 12));
        } else {
            RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(station.getFlagship());
            RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, station, null, 3, randomSeed);

            system.addScript(new RemnantStationFleetManager(station, 1f, 0, 8 + randomSeed.nextInt(5), 15f, 8, 24));
        }

        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

        return station;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.AsteroidFieldGenPlugin's generate() for vanilla implementation
    private SectorEntityToken addAsteroidField(JSONObject options) {
        String name = options.optString(OPT_NAME, null);
        float radius = options.optInt(OPT_SIZE, 400);

        int count = options.optInt(OPT_NUM_ASTEROIDS, DEFAULT_SET_TO_PROC_GEN);
        if (count < 0) {
            count = (int) (radius * radius * 3.14f / 80000f);
            if (count < 10) count = 10;
            if (count > 100) count = 100;
        }

        return system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(radius, radius + 100f, count, count, 4f, 16f, name));
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemGenerator's addDebrisField() for vanilla implementation
    private SectorEntityToken addDebrisField(JSONObject options) {
        float radius = options.optInt(OPT_SIZE, 500);

        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(radius, -1f, 10000000f, 0f);

        return Misc.addDebrisField(system, params, randomSeed);
    }

    // See com.fs.starfarer.api.impl.campaign.proc.themes.BaseThemeGenerator's addDerelictShip() for vanilla implementation
    private SectorEntityToken addDerelictShip(JSONObject options, int index) throws JSONException {
        String factionId = options.optString(OPT_FACTION_ID, Factions.INDEPENDENT);
        String variantId = options.optString(OPT_VARIANT_ID, null);
        SectorEntityToken entity;
        if (variantId == null)
            entity = addSalvageEntity(randomSeed, system, Entities.WRECK, Factions.NEUTRAL, DerelictShipEntityPlugin.createRandom(factionId, null, randomSeed, DerelictShipEntityPlugin.getDefaultSModProb()));
        else
            entity = addSalvageEntity(randomSeed, system, Entities.WRECK, Factions.NEUTRAL, DerelictShipEntityPlugin.createVariant(variantId, randomSeed, DerelictShipEntityPlugin.getDefaultSModProb()));
        entity.setDiscoverable(true);
        setEntityLocation(entity, options, index);
        return entity;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.AccretionDiskGenPlugin's generate() for vanilla implementation
    private SectorEntityToken addAccretionDisk(JSONObject options, int index) {
        SectorEntityToken focusEntity = getFocusEntity(options, index);
        float orbitRadius = options.optInt(OPT_ORBIT_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitRadius <= 0) orbitRadius = Math.max(focusEntity.getRadius() * 5f, 90f) * (2f + randomSeed.nextFloat());

        float bandWidth = 256f;
        int numBands = 12;
        for (int i = 0; i < numBands; i++) {
            float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;
            String ring = randomSeed.nextBoolean() ? ENTITY_RINGS_ICE : ENTITY_RINGS_DUST;
            int ringIndex = randomSeed.nextInt(2);
            RingBandAPI visual = system.addRingBand(focusEntity, CATEGORY_MISC, ring, 256f, ringIndex, new Color(46, 35, 173), bandWidth, radius + bandWidth / 2f, -(radius / (30f + 10f * randomSeed.nextFloat())));
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0);
            visual.setSpiralFactor(2f + randomSeed.nextFloat() * 5f);
        }

        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius, orbitRadius / 2f, focusEntity, DEFAULT_ACCRETION_DISK_NAME));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(focusEntity, 0, 0, -100);
        return ring;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.MagFieldGenPlugin's generate() for vanilla implementation
    private SectorEntityToken addMagneticField(JSONObject options, int index) throws JSONException {
        SectorEntityToken focusEntity = getFocusEntity(options, index);
        float orbitRadius = options.getInt(OPT_ORBIT_RADIUS); // Inner radius, or visual band start

        float bandWidth = options.optInt(OPT_SIZE, 400);
        float middleRadius = options.optInt(OPT_MIDDLE_RADIUS, (int) (orbitRadius + bandWidth / 2f));
        float outerRadius = options.optInt(OPT_OUTER_RADIUS, (int) (orbitRadius + bandWidth));

        Color baseColor = getColor(options.optJSONArray(OPT_BASE_COLOR));
        if (baseColor == null) baseColor = new Color(50, 20, 100, 40);

        float auroraFrequency = (float) options.optDouble(OPT_AURORA_FREQUENCY, DEFAULT_SET_TO_PROC_GEN);
        if (auroraFrequency < 0) auroraFrequency = 0.25f + 0.75f * randomSeed.nextFloat();

        Color[] auroraColors = getColors(options.optJSONArray(OPT_AURORA_COLOR));
        if (auroraColors == null)
            auroraColors = new Color[]{new Color(140, 100, 235), new Color(180, 110, 210), new Color(150, 140, 190), new Color(140, 190, 210), new Color(90, 200, 170), new Color(65, 230, 160), new Color(20, 220, 70)};

        SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(bandWidth, middleRadius, focusEntity, orbitRadius, outerRadius, baseColor, auroraFrequency, auroraColors));
        field.setCircularOrbit(focusEntity, 0, 0, 100f);
        return field;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.RingGenPlugin's generate() for vanilla implementation
    private SectorEntityToken addRingBand(JSONObject options, int index) throws JSONException {
        SectorEntityToken focusEntity = getFocusEntity(options, index);
        float orbitRadius = options.getInt(OPT_ORBIT_RADIUS);

        float orbitDays = options.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0) orbitDays = orbitRadius / (15f + 5f * randomSeed.nextFloat());

        if (!options.optBoolean(OPT_ORBIT_CLOCKWISE, true)) orbitDays *= -1f;

        String name = options.optString(OPT_NAME, null);
        String type = options.optString(OPT_TYPE, ENTITY_RINGS_DUST);

        int bandIndex = options.optInt(OPT_BAND_INDEX, 1);

        if (type.equals(ENTITY_RINGS_SPECIAL) ? bandIndex != 1 : (bandIndex < 0 || bandIndex > 3)) bandIndex = 1;
        return system.addRingBand(focusEntity, CATEGORY_MISC, type, 256f, bandIndex, Color.white, 256f, orbitRadius, orbitDays, Terrain.RING, name);
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.AsteroidBeltGenPlugin's generate() for vanilla implementation
    private SectorEntityToken addAsteroidBelt(JSONObject options, int index) throws JSONException {
        SectorEntityToken focusEntity = getFocusEntity(options, index);
        float orbitRadius = options.getInt(OPT_ORBIT_RADIUS);

        float orbitDays = options.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0) orbitDays = orbitRadius / (15f + 5f * randomSeed.nextFloat());

        int count = options.optInt(OPT_NUM_ASTEROIDS, DEFAULT_SET_TO_PROC_GEN);
        if (count < 0) {
            count = (int) (orbitDays * (0.25f + 0.5f * randomSeed.nextFloat()));
            if (count > 100) count = (int) (100f + (count - 100f) * 0.25f);
            if (count > 250) count = 250;
        }

        if (!options.optBoolean(OPT_ORBIT_CLOCKWISE, true)) orbitDays *= -1f;

        int innerBandIndex = options.optInt(OPT_INNER_BAND_INDEX, 0);
        int outerBandIndex = options.optInt(OPT_OUTER_BAND_INDEX, 0);
        if (innerBandIndex < 0 || innerBandIndex > 3) innerBandIndex = 0;
        if (outerBandIndex < 0 || outerBandIndex > 3) outerBandIndex = 0;

        String name = options.optString(OPT_NAME, null);
        float width = options.optInt(OPT_SIZE, 256);
        system.addRingBand(focusEntity, CATEGORY_MISC, ENTITY_RINGS_ASTEROIDS, 256f, innerBandIndex, Color.white, 256f, orbitRadius - width * 0.25f, orbitDays * 1.05f, null, null);
        system.addRingBand(focusEntity, CATEGORY_MISC, ENTITY_RINGS_ASTEROIDS, 256f, outerBandIndex, Color.white, 256f, orbitRadius + width * 0.25f, orbitDays, null, null);
        return system.addAsteroidBelt(focusEntity, count, orbitRadius, width, orbitDays * .75f, orbitDays * 1.5f, Terrain.ASTEROID_BELT, name);
    }

    private SectorEntityToken addCustomEntity(JSONObject options) throws JSONException {
        String name = options.optString(OPT_NAME, null);
        String type = options.getString(OPT_ENTITY);
        String factionId = options.optString(OPT_FACTION_ID, null);
        SectorEntityToken entity;
        try {
            entity = system.addCustomEntity(null, name, type, factionId);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(String.format(ERROR_INVALID_ENTITY_ID, type, SYSTEM_ID)), e);
        }

        switch (type) {
            case Entities.COMM_RELAY:
            case Entities.COMM_RELAY_MAKESHIFT:
            case Entities.NAV_BUOY:
            case Entities.NAV_BUOY_MAKESHIFT:
            case Entities.SENSOR_ARRAY:
            case Entities.SENSOR_ARRAY_MAKESHIFT:
                if (factionId == null || factionId.equals(Factions.NEUTRAL))
                    entity.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
                break;
            case Entities.CORONAL_TAP:
                system.addScript(new CoronalTapParticleScript(entity));
                break;
            case Entities.DERELICT_CRYOSLEEPER:
                entity.setFaction(Factions.DERELICT);
                break;
        }

        SalvageEntityGenDataSpec salvageData = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, type, true);
        if (salvageData != null) {
            entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, randomSeed.nextLong());
            makeDiscoverable(entity, salvageData.getDetectionRange());
        }

        addCustomDescription(entity, options);

        return entity;
    }

    private void addMemoryKeys(HasMemory entity, JSONObject entityOptions, String optionKey) {
        JSONObject memoryKeys = entityOptions.optJSONObject(optionKey);
        if (memoryKeys != null) for (Iterator<String> it = memoryKeys.keys(); it.hasNext(); ) {
            String memKey = it.next();

            try { // Try-catch because it's otherwise impossible to distinguish the source of optBoolean()'s false value
                entity.getMemoryWithoutUpdate().set(memKey, memoryKeys.getBoolean(memKey));
            } catch (JSONException e) {
                entity.getMemoryWithoutUpdate().set(memKey, memoryKeys.optString(memKey, null));
            }
        }
    }

    private void addTags(SectorEntityToken entity, JSONObject entityOptions) throws JSONException {
        JSONArray tags = entityOptions.optJSONArray(OPT_TAGS);
        if (tags != null) for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            entity.addTag(tag);
        }
    }

    private SectorEntityToken getFocusEntity(JSONObject entityOptions, int index) {
        int focus = entityOptions.optInt(OPT_FOCUS);
        if (focus >= systemEntities.size())
            throw new IllegalArgumentException(String.format(ERROR_INVALID_FOCUS, SYSTEM_ID, index));
        return systemEntities.get(focus);
    }

    private void setPlanetConditions(PlanetAPI planet, JSONObject planetOptions) throws JSONException {
        Misc.initConditionMarket(planet);
        MarketAPI planetMarket = planet.getMarket();
        addMemoryKeys(planetMarket, planetOptions, OPT_MARKET_MEMORY_KEYS);

        JSONArray conditions = planetOptions.optJSONArray(OPT_CONDITIONS);
        if (conditions != null) for (int i = 0; i < conditions.length(); i++)
            try {
                planetMarket.addCondition(conditions.getString(i));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_CONDITION_UNINHABITED, conditions.getString(i), planet.getTypeId(), SYSTEM_ID), e);
            }
    }

    private void setStationConditions(SectorEntityToken station, JSONObject stationOptions) throws JSONException {
        Misc.setAbandonedStationMarket(station.getId(), station);
        MarketAPI stationMarket = station.getMarket();
        addMemoryKeys(stationMarket, stationOptions, OPT_MARKET_MEMORY_KEYS);

        JSONArray conditions = stationOptions.optJSONArray(OPT_CONDITIONS);
        if (conditions != null) for (int i = 0; i < conditions.length(); i++)
            try {
                stationMarket.addCondition(conditions.getString(i));
            } catch (Exception e) { // Error message still assume planets, but whatever
                throw new IllegalArgumentException(String.format(ERROR_INVALID_CONDITION_UNINHABITED, conditions.getString(i), station.getCustomEntityType(), SYSTEM_ID), e);
            }
    }

    private void addMarket(SectorEntityToken entity, JSONObject marketOptions, int size) throws JSONException {
        // Default to population 10 if no mods adding bigger population sizes exist
        if (size > 10 && Global.getSettings().getMarketConditionSpec(CONDITION_POPULATION + size) == null) size = 10;

        String factionId = marketOptions.getString(OPT_FACTION_ID);

        // Similar to initEconomyMarket() in Misc.java
        MarketAPI entityMarket = Global.getFactory().createMarket(ID_MARKET + entity.getId(), entity.getName(), size);
        addMemoryKeys(entityMarket, marketOptions, OPT_MARKET_MEMORY_KEYS);

        entityMarket.setPrimaryEntity(entity);
        entityMarket.setFactionId(factionId);
        entityMarket.getTariff().setBaseValue(0.3f); // Default tariff value
        entityMarket.setFreePort(marketOptions.optBoolean(OPT_FREE_PORT, false));

        entityMarket.addCondition(CONDITION_POPULATION + size);
        JSONArray conditions = marketOptions.optJSONArray(OPT_CONDITIONS);
        if (conditions != null) for (int i = 0; i < conditions.length(); i++)
            try {
                entityMarket.addCondition(conditions.getString(i));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_CONDITION_INHABITED, conditions.getString(i), size, factionId, SYSTEM_ID), e);
            }

        JSONArray industries = marketOptions.optJSONArray(OPT_INDUSTRIES);
        if (industries != null) {
            for (int i = 0; i < industries.length(); i++) {
                JSONArray specials = industries.optJSONArray(i);
                String industryId;
                if (specials != null) industryId = specials.getString(0);
                else industryId = industries.optString(i, null);

                try {
                    entityMarket.addIndustry(industryId);
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format(ERROR_INVALID_INDUSTRY, industryId, size, factionId, SYSTEM_ID), e);
                }

                if (specials != null && specials.length() > 1) {
                    Industry newIndustry = entityMarket.getIndustry(industryId);

                    String aiCoreId = specials.optString(1, null);
                    if (aiCoreId != null) newIndustry.setAICoreId(aiCoreId);

                    String specialItem = specials.optString(2, null);
                    if (specialItem != null) newIndustry.setSpecialItem(new SpecialItemData(specialItem, null));

                    if (specials.length() > 3) newIndustry.setImproved(specials.optBoolean(3, false));
                }
            }

            // "Population & Infrastructure" industry must exist for colonies to work properly
            if (!entityMarket.hasIndustry(Industries.POPULATION)) entityMarket.addIndustry(Industries.POPULATION);
        } else { // Just give market the bare minimum colony
            entityMarket.addIndustry(Industries.POPULATION);
            entityMarket.addIndustry(Industries.SPACEPORT);
        }

        entityMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        if (factionId.equals(Factions.PLAYER)) {
            entityMarket.setPlayerOwned(true);
            entityMarket.addSubmarket(Submarkets.LOCAL_RESOURCES);
            ((StoragePlugin) entityMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            marketsToOverrideAdmin.put(entityMarket, Factions.PLAYER);
        } else {
            entityMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
            if (Misc.isMilitary(entityMarket) || entityMarket.hasIndustry(Industries.MILITARYBASE) || entityMarket.hasIndustry(Industries.HIGHCOMMAND))
                entityMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
            entityMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);

            if (initPeople) createInitialPeople(entityMarket, randomSeed);
        }

        if (marketOptions.optBoolean(OPT_AI_CORE_ADMIN, false))
            marketsToOverrideAdmin.put(entityMarket, Commodities.ALPHA_CORE);

        entity.setFaction(factionId); // Needed to allow interacting with faction market
        entity.setMarket(entityMarket);
        Global.getSector().getEconomy().addMarket(entityMarket, true);

        hasFactionPresence = true;
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator's addSolarShadesAndMirrors() for vanilla implementation
    private void addSolarArrayIfApplicable(PlanetAPI planet) {
        if (!planet.hasCondition(Conditions.SOLAR_ARRAY)) return;

        int numOfShades = 0;
        int numOfMirrors = 0;
        String planetType = planet.getTypeId();
        StarSystemAPI system = planet.getStarSystem();
        String starType = system.getStar().getTypeId();
        if (planet.hasCondition(Conditions.HOT) || planetType.equals(Planets.DESERT) || planetType.equals(Planets.DESERT1) || planetType.equals(Planets.ARID) || starType.equals(StarTypes.BLUE_GIANT) || starType.equals(StarTypes.BLUE_SUPERGIANT))
            numOfShades = randomSeed.nextBoolean() ? 3 : 1;

        if (planet.hasCondition(Conditions.POOR_LIGHT) || planetType.equals(Planets.PLANET_TERRAN_ECCENTRIC) || starType.equals(StarTypes.RED_DWARF) || starType.equals(StarTypes.BROWN_DWARF))
            numOfMirrors = randomSeed.nextBoolean() ? 5 : 3;

        // Force a solar array if none of the above conditions are met
        if (numOfShades == 0 && numOfMirrors == 0) {
            numOfShades = 1;
            numOfMirrors = 3;
        }

        String factionId = planet.getFaction().getId();
        float period = planet.getCircularOrbitPeriod();
        float angle = planet.getCircularOrbitAngle();
        float radius = 270f + planet.getRadius();

        if (numOfShades >= 1) {
            SectorEntityToken shade2 = system.addCustomEntity(null, NAME_SHADE2, Entities.STELLAR_SHADE, factionId);
            shade2.setCircularOrbitPointingDown(planet, angle + 180, radius + 25, period);
            if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE))
                MiscellaneousThemeGenerator.makeDiscoverable(shade2, 300f, 2000f);

            if (numOfShades == 3) {
                SectorEntityToken shade1 = system.addCustomEntity(null, NAME_SHADE1, Entities.STELLAR_SHADE, factionId);
                SectorEntityToken shade3 = system.addCustomEntity(null, NAME_SHADE3, Entities.STELLAR_SHADE, factionId);
                shade1.setCircularOrbitPointingDown(planet, angle + 154, radius - 10, period);
                shade3.setCircularOrbitPointingDown(planet, angle + 206, radius - 10, period);
                if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                    MiscellaneousThemeGenerator.makeDiscoverable(shade1, 300f, 2000f);
                    MiscellaneousThemeGenerator.makeDiscoverable(shade3, 300f, 2000f);
                }
            }
        }

        if (numOfMirrors >= 3) {
            SectorEntityToken mirror2 = system.addCustomEntity(null, NAME_MIRROR2, Entities.STELLAR_MIRROR, factionId);
            SectorEntityToken mirror3 = system.addCustomEntity(null, NAME_MIRROR3, Entities.STELLAR_MIRROR, factionId);
            SectorEntityToken mirror4 = system.addCustomEntity(null, NAME_MIRROR4, Entities.STELLAR_MIRROR, factionId);
            mirror2.setCircularOrbitPointingDown(planet, angle - 30, radius, period);
            mirror3.setCircularOrbitPointingDown(planet, angle, radius, period);
            mirror4.setCircularOrbitPointingDown(planet, angle + 30, radius, period);
            if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                MiscellaneousThemeGenerator.makeDiscoverable(mirror2, 300f, 2000f);
                MiscellaneousThemeGenerator.makeDiscoverable(mirror3, 300f, 2000f);
                MiscellaneousThemeGenerator.makeDiscoverable(mirror4, 300f, 2000f);
            }

            if (numOfMirrors == 5) {
                SectorEntityToken mirror1 = system.addCustomEntity(null, NAME_MIRROR1, Entities.STELLAR_MIRROR, factionId);
                SectorEntityToken mirror5 = system.addCustomEntity(null, NAME_MIRROR5, Entities.STELLAR_MIRROR, factionId);
                mirror1.setCircularOrbitPointingDown(planet, angle - 60, radius, period);
                mirror5.setCircularOrbitPointingDown(planet, angle + 60, radius, period);
                if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                    MiscellaneousThemeGenerator.makeDiscoverable(mirror1, 300f, 2000f);
                    MiscellaneousThemeGenerator.makeDiscoverable(mirror5, 300f, 2000f);
                }
            }
        }
    }

    private String getProcGenName(String tag, String parent) {
        String name = ProcgenUsedNames.pickName(tag, parent, null).nameWithRomanSuffixIfAny;
        ProcgenUsedNames.notifyUsed(name);
        return name;
    }

    private void addCustomDescription(SectorEntityToken entity, JSONObject entityOptions) {
        String id = entityOptions.optString(OPT_CUSTOM_DESCRIPTION_ID, null);
        if (id != null) entity.setCustomDescriptionId(id);
    }

    // Note: Textures must already be preloaded via settings.json for texture-replacing fields to work
    // Partially adapted from Tartiflette's Unknown Skies source code
    private void addSpecChanges(PlanetAPI body, JSONObject specOptions) throws JSONException {
        if (specOptions == null) return;
        PlanetSpecAPI bodySpec = body.getSpec();

        JSONArray atmosphereColor = specOptions.optJSONArray(OPT_ATMOSPHERE_COLOR);
        if (atmosphereColor != null) bodySpec.setAtmosphereColor(getColor(atmosphereColor));

        float atmosphereThickness = (float) specOptions.optDouble(OPT_ATMOSPHERE_THICKNESS);
        if (!Float.isNaN(atmosphereThickness)) bodySpec.setAtmosphereThickness(atmosphereThickness);

        float atmosphereThicknessMin = (float) specOptions.optDouble(OPT_ATMOSPHERE_THICKNESS_MIN);
        if (!Float.isNaN(atmosphereThicknessMin)) bodySpec.setAtmosphereThicknessMin(atmosphereThicknessMin);

        JSONArray cloudColor = specOptions.optJSONArray(OPT_CLOUD_COLOR);
        if (cloudColor != null) bodySpec.setCloudColor(getColor(cloudColor));

        float cloudRotation = (float) specOptions.optDouble(OPT_CLOUD_ROTATION);
        if (!Float.isNaN(cloudRotation)) bodySpec.setCloudRotation(cloudRotation);

        String cloudTexture = specOptions.optString(OPT_CLOUD_TEXTURE, null);
        if (cloudTexture != null) bodySpec.setCloudTexture(PATH_GRAPHICS_PLANET + cloudTexture);

        JSONArray glowColor = specOptions.optJSONArray(OPT_GLOW_COLOR);
        if (glowColor != null) bodySpec.setGlowColor(getColor(glowColor));

        String glowTexture = specOptions.optString(OPT_GLOW_TEXTURE, null);
        if (glowTexture != null) bodySpec.setGlowTexture(PATH_GRAPHICS_PLANET + glowTexture);

        JSONArray iconColor = specOptions.optJSONArray(OPT_ICON_COLOR);
        if (iconColor != null) bodySpec.setIconColor(getColor(iconColor));

        float pitch = (float) specOptions.optDouble(OPT_PITCH);
        if (!Float.isNaN(pitch)) bodySpec.setPitch(pitch);

        JSONArray planetColor = specOptions.optJSONArray(OPT_PLANET_COLOR);
        if (planetColor != null) bodySpec.setPlanetColor(getColor(planetColor));

        float rotation = (float) specOptions.optDouble(OPT_ROTATION);
        if (!Float.isNaN(rotation)) bodySpec.setRotation(rotation);

        String texture = specOptions.optString(OPT_TEXTURE, null);
        if (texture != null) bodySpec.setTexture(PATH_GRAPHICS_PLANET + texture);

        float tilt = (float) specOptions.optDouble(OPT_TILT);
        if (!Float.isNaN(tilt)) bodySpec.setTilt(tilt);

        boolean revLightGlow = specOptions.optBoolean(OPT_USE_REVERSE_LIGHT_FOR_GLOW);
        if (revLightGlow != bodySpec.isUseReverseLightForGlow()) bodySpec.setUseReverseLightForGlow(revLightGlow);

        JSONArray typeOverride = specOptions.optJSONArray(OPT_TYPE_OVERRIDE);
        if (typeOverride != null) {
            String newType = typeOverride.getString(0);
            ((PlanetSpec) bodySpec).planetType = newType;
            ((PlanetSpec) bodySpec).name = typeOverride.getString(1);
            ((PlanetSpec) bodySpec).descriptionId = newType;
            body.setTypeId(newType);
        }

        body.applySpecChanges();
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator's addCryosleeper() for vanilla implementation
    private void generateDomainCryosleeperIfApplicable() {
        if (!SYSTEM_OPTIONS.optBoolean(OPT_ADD_DOMAIN_CRYOSLEEPER, false)) return;

        float orbitRadius = systemRadius + 1000f;
        SectorEntityToken cryosleeper = system.addCustomEntity(null, DEFAULT_CRYOSLEEPER_NAME, Entities.DERELICT_CRYOSLEEPER, Factions.DERELICT);
        cryosleeper.setCircularOrbitWithSpin(system.getCenter(), randomSeed.nextFloat() * 360f, orbitRadius, orbitRadius / (15f + randomSeed.nextFloat() * 5f), 1f, 11);
        cryosleeper.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, randomSeed.nextLong());

        if (!hasFactionPresence) makeDiscoverable(cryosleeper, 3500f);
    }

    // See com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator's addCoronalTaps() for vanilla implementation
    private void generateCoronalHypershuntIfApplicable() {
        if (!SYSTEM_OPTIONS.optBoolean(OPT_ADD_CORONAL_HYPERSHUNT, false)) return;

        SectorEntityToken systemCenter = system.getCenter();
        SectorEntityToken hypershunt = system.addCustomEntity(null, null, Entities.CORONAL_TAP, null);
        if (systemCenter.isStar()) { // Orbit the sole star
            float orbitRadius = systemCenter.getRadius() + hypershunt.getRadius() + 100f;
            hypershunt.setCircularOrbitPointingDown(systemCenter, randomSeed.nextFloat() * 360f, orbitRadius, orbitRadius / 20f);
        } else { // Stay in the center, facing towards the primary star
            hypershunt.setLocation(0, 0); // May not be necessary, but better safe than sorry
            system.addScript(new MiscellaneousThemeGenerator.MakeCoronalTapFaceNearestStar(hypershunt));
        }

        if (!hasFactionPresence) makeDiscoverable(hypershunt, 3500f);

        system.addScript(new CoronalTapParticleScript(hypershunt));
    }

    private void makeDiscoverable(SectorEntityToken entity, float detectedRange) {
        entity.setSensorProfile(1f);
        entity.setDiscoverable(true);
        entity.getDetectedRangeMod().modifyFlat(SOURCE_GEN, detectedRange);
    }

    private void addMusicIfApplicable() {
        String musicId = SYSTEM_OPTIONS.optString(OPT_SYSTEM_MUSIC, null);
        if (musicId != null) system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, musicId);
    }

    private void addNebulaIfApplicable() {
        if (SYSTEM_OPTIONS.optBoolean(OPT_HAS_SYSTEMWIDE_NEBULA, false))
            StarSystemGenerator.addSystemwideNebula(system, system.getAge());
    }

    private void setBackground() {
        String bgFileName = SYSTEM_OPTIONS.optString(OPT_SYSTEM_BACKGROUND, null);
        if (bgFileName != null) system.setBackgroundTextureFilename(PATH_GRAPHICS_BACKGROUND + bgFileName);
        else {
            String nebulaType = system.hasSystemwideNebula() ? StarSystemGenerator.nebulaTypes.get(system.getAge()) : StarSystemGenerator.NEBULA_NONE;
            system.setBackgroundTextureFilename(StarSystemGenerator.backgroundsByNebulaType.get(nebulaType).pick());
        }
    }

    private void addSystemTagsIfApplicable() throws JSONException {
        JSONArray systemTags = SYSTEM_OPTIONS.optJSONArray(OPT_SYSTEM_TAGS);
        if (systemTags != null) for (int i = 0; i < systemTags.length(); i++)
            system.addTag(systemTags.getString(i));

        // So it does not always get tagged as a Core World system when created at new game
        if (system.getTags().isEmpty()) system.addTag(Tags.THEME_MISC);
    }

    private void setLightColor() throws JSONException {
        Color result = getColor(SYSTEM_OPTIONS.optJSONArray(OPT_SYSTEM_LIGHT_COLOR));
        if (result == null) {
            result = Color.WHITE;
            List<PlanetAPI> planetList = system.getPlanets();
            for (int i = 0; i < planetList.size(); i++)
                if (planetList.get(i).isStar())
                    if (i != 0) result = Misc.interpolateColor(result, pickLightColorForStar(planetList.get(i)), 0.5f);
                    else result = pickLightColorForStar(planetList.get(i));
        }
        system.setLightColor(result);
    }

    private Color pickLightColorForStar(PlanetAPI star) {
        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), true);
        return Misc.interpolateColor(starData.getLightColorMin(), starData.getLightColorMax(), randomSeed.nextFloat());
    }

    private void generateHyperspace() {
        system.autogenerateHyperspaceJumpPoints(true, !hasJumpPoint);

        // Clear nebula in hyperspace
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float totalRadius = system.getMaxRadiusInHyperspace() + plugin.getTileSize() * 2f;
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, totalRadius, 0f, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, totalRadius, 0f, 360f, 0.25f);
    }

    private void addRemnantWarningBeaconsIfApplicable() {
        if (system.hasTag(Tags.THEME_REMNANT_RESURGENT))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.RESURGENT);
        else if (system.hasTag(Tags.THEME_REMNANT_SUPPRESSED))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.SUPPRESSED);
        else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.DESTROYED);
    }

    private Color getColor(JSONArray rgba) throws JSONException {
        if (rgba == null) return null;
        return new Color(rgba.getInt(0), rgba.getInt(1), rgba.getInt(2), rgba.getInt(3));
    }

    private Color[] getColors(JSONArray colorList) throws JSONException {
        if (colorList == null) return null;

        Color[] colors = new Color[colorList.length()];
        for (int i = 0; i < colorList.length(); i++) colors[i] = getColor(colorList.getJSONArray(i));

        return colors;
    }

    private void setLocation() throws JSONException {
        JSONArray locationOverride = SYSTEM_OPTIONS.optJSONArray(OPT_SET_LOCATION);
        if (locationOverride == null)
            setConstellationLocation(systemRadius / 10f + Global.getSettings().getInt(SETTINGS_SYSTEM_SPACING), SYSTEM_OPTIONS.optInt(OPT_SET_LOCATION, 0));
        else setLocation(locationOverride.getInt(0), locationOverride.getInt(1));

        // Set system age
        switch (SYSTEM_OPTIONS.optString(OPT_SYSTEM_AGE, "")) {
            case "old":
                system.setAge(StarAge.OLD);
                break;
            case "average":
                system.setAge(StarAge.AVERAGE);
                break;
            case "young":
                system.setAge(StarAge.YOUNG);
                break;
        }
    }

    private void setLocation(float x, float y) {
        system.getLocation().set(x, y);
        system.setAge(StarAge.AVERAGE); // Can't be ANY, since game will crash if addSystemwideNebula() is ran
    }

    // Set a star system's location to near the middle of a specified constellation
    // Modified from the constellation proc-gen code originally made by Audax.
    private void setConstellationLocation(float hyperspaceRadius, int index) {
        // If no constellations exist (for whatever reason), fallback to the hyperspace center
        if (constellations.isEmpty()) {
            Vector2f hyperspaceCenter = CSSUtil.getHyperspaceCenter();
            setLocation(hyperspaceCenter.getX(), hyperspaceCenter.getY());
            return;
        }

        Constellation selectedConstellation = (index <= 0) ? constellations.get(randomSeed.nextInt(constellations.size())) : constellations.get(Math.min(index, constellations.size()) - 1);

        float centroidX = 0;
        float centroidY = 0;
        List<StarSystemAPI> nearestSystems = selectedConstellation.getSystems();
        for (StarSystemAPI sys : nearestSystems) {
            Vector2f loc = sys.getHyperspaceAnchor().getLocationInHyperspace();
            centroidX += loc.getX();
            centroidY += loc.getY();
        }
        centroidX /= nearestSystems.size();
        centroidY /= nearestSystems.size();

        // Nudge the centroid point to a nearby random location within an 8000 unit square box
        centroidX += randomSeed.nextFloat() * 8000f - 4000f;
        centroidY += randomSeed.nextFloat() * 8000f - 4000f;

        // Find an empty spot in the constellation, starting at the centroid point and
        // then searching for locations around it in a square pattern
        Vector2f newLoc = null;
        int curX = 0;
        int curY = 0;
        int squareSize = 0;
        byte move = 3; // 0 = left, 1 = down; 2 = right; 3 = up
        while (newLoc == null) {
            float thisX = curX * 25f + centroidX;
            float thisY = curY * 25f + centroidY;
            boolean intersects = false;
            for (StarSystemAPI sys : nearestSystems) {
                Vector2f sysLoc = sys.getHyperspaceAnchor().getLocation();
                float dX = thisX - sysLoc.getX();
                float dY = thisY - sysLoc.getY();
                float dR = hyperspaceRadius + sys.getMaxRadiusInHyperspace();
                if (dX * dX + dY * dY < dR * dR) { // Formula to check if two circular areas intersect
                    intersects = true;
                    break;
                }
            }

            if (!intersects) newLoc = new Vector2f(thisX, thisY);
            else switch (move) {
                case 0: // Moving left
                    if (curX != -squareSize) curX--;
                    else {
                        move = 1;
                        curY--;
                    }
                    break;
                case 1: // Moving down
                    if (curY != -squareSize) curY--;
                    else {
                        move = 2;
                        curX++;
                    }
                    break;
                case 2: // Moving right
                    if (curX != squareSize) curX++;
                    else {
                        move = 3;
                        curY++;
                    }
                    break;
                case 3: // Moving up
                    if (curY != squareSize) curY++;
                    else { // Checked the full perimeter, so increase search size
                        squareSize++;
                        curX = squareSize - 1;
                        curY = squareSize;
                        move = 0;
                    }
                    break;
            }
        }

        nearestSystems.add(system);
        system.setConstellation(selectedConstellation);
        system.getLocation().set(newLoc);
        system.setAge(selectedConstellation.getAge());
    }
}