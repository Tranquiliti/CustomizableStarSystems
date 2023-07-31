package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
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

/**
 * A utility class for the Customizable Star Systems mod
 */
public class CSSUtil {
    // Default values
    public final int DEFAULT_FOCUS = 0;
    public final int DEFAULT_SET_TO_PROC_GEN = -1; // For user's sake, exampleSettings.json uses 0 to specify proc-gen
    public final int DEFAULT_MARKET_SIZE = 0;
    public final int DEFAULT_FRINGE_ORBIT_RADIUS = 5000;

    // Main system options in customStarSystems.json
    public final String OPT_IS_ENABLED = Global.getSettings().getString("customizablestarsystems", "opt_isEnabled");
    public final String OPT_NUMBER_OF_SYSTEMS = Global.getSettings().getString("customizablestarsystems", "opt_numberOfSystems");
    public final String OPT_IS_CORE_WORLD_SYSTEM = Global.getSettings().getString("customizablestarsystems", "opt_isCoreWorldSystem");
    public final String OPT_HAS_SYSTEMWIDE_NEBULA = Global.getSettings().getString("customizablestarsystems", "opt_hasSystemwideNebula");
    public final String OPT_ADD_CORONAL_HYPERSHUNT = Global.getSettings().getString("customizablestarsystems", "opt_addCoronalHypershunt");
    public final String OPT_ADD_DOMAIN_CRYOSLEEPER = Global.getSettings().getString("customizablestarsystems", "opt_addDomainCryosleeper");
    public final String OPT_SET_LOCATION = Global.getSettings().getString("customizablestarsystems", "opt_setLocation");
    public final String OPT_SET_LOCATION_OVERRIDE = Global.getSettings().getString("customizablestarsystems", "opt_setLocationOverride");
    public final String OPT_SYSTEM_BACKGROUND = Global.getSettings().getString("customizablestarsystems", "opt_systemBackground");
    public final String OPT_SYSTEM_MUSIC = Global.getSettings().getString("customizablestarsystems", "opt_systemMusic");
    public final String OPT_FRINGE_JUMP_POINT = Global.getSettings().getString("customizablestarsystems", "opt_fringeJumpPoint");
    public final String OPT_STARS_IN_SYSTEM_CENTER = Global.getSettings().getString("customizablestarsystems", "opt_starsInSystemCenter");
    public final String OPT_ORBITING_BODIES = Global.getSettings().getString("customizablestarsystems", "opt_orbitingBodies");
    public final String OPT_SYSTEM_FEATURES = Global.getSettings().getString("customizablestarsystems", "opt_systemFeatures");

    // Sub-options
    public final String OPT_ORBIT_RADIUS = Global.getSettings().getString("customizablestarsystems", "opt_orbitRadius");
    public final String OPT_NAME = Global.getSettings().getString("customizablestarsystems", "opt_name");
    public final String OPT_ORBIT_ANGLE = Global.getSettings().getString("customizablestarsystems", "opt_orbitAngle");
    public final String OPT_ORBIT_DAYS = Global.getSettings().getString("customizablestarsystems", "opt_orbitDays");
    public final String OPT_STARS = Global.getSettings().getString("customizablestarsystems", "opt_stars");
    public final String OPT_TYPE = Global.getSettings().getString("customizablestarsystems", "opt_type");
    public final String OPT_RADIUS = Global.getSettings().getString("customizablestarsystems", "opt_radius");
    public final String OPT_CORONA_RADIUS = Global.getSettings().getString("customizablestarsystems", "opt_coronaRadius");
    public final String OPT_FLARE_CHANCE = Global.getSettings().getString("customizablestarsystems", "opt_flareChance");
    public final String OPT_SPEC_CHANGES = Global.getSettings().getString("customizablestarsystems", "opt_specChanges");
    public final String OPT_CUSTOM_DESCRIPTION_ID = Global.getSettings().getString("customizablestarsystems", "opt_customDescriptionId");
    public final String OPT_FOCUS = Global.getSettings().getString("customizablestarsystems", "opt_focus");
    public final String OPT_CONDITIONS = Global.getSettings().getString("customizablestarsystems", "opt_conditions");
    public final String OPT_ENTITIES_AT_STABLE_POINTS = Global.getSettings().getString("customizablestarsystems", "opt_entitiesAtStablePoints");
    public final String OPT_PLANET_OPTIONS = Global.getSettings().getString("customizablestarsystems", "opt_planetOptions");
    public final String OPT_STATION_TYPE = Global.getSettings().getString("customizablestarsystems", "opt_stationType");
    public final String OPT_MARKET_SIZE = Global.getSettings().getString("customizablestarsystems", "opt_marketSize");
    public final String OPT_FACTION_ID = Global.getSettings().getString("customizablestarsystems", "opt_factionId");
    public final String OPT_FREE_PORT = Global.getSettings().getString("customizablestarsystems", "opt_freePort");
    public final String OPT_AI_CORE_ADMIN = Global.getSettings().getString("customizablestarsystems", "opt_aiCoreAdmin");
    public final String OPT_INDUSTRIES = Global.getSettings().getString("customizablestarsystems", "opt_industries");

    // Sub-options for "specChanges"
    public final String OPT_ATMOSPHERE_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_atmosphereColor");
    public final String OPT_ATMOSPHERE_THICKNESS = Global.getSettings().getString("customizablestarsystems", "opt_atmosphereThickness");
    public final String OPT_ATMOSPHERE_THICKNESS_MIN = Global.getSettings().getString("customizablestarsystems", "opt_atmosphereThicknessMin");
    public final String OPT_CLOUD_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_cloudColor");
    public final String OPT_CLOUD_ROTATION = Global.getSettings().getString("customizablestarsystems", "opt_cloudRotation");
    public final String OPT_CLOUD_TEXTURE = Global.getSettings().getString("customizablestarsystems", "opt_cloudTexture");
    public final String OPT_GLOW_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_glowColor");
    public final String OPT_GLOW_TEXTURE = Global.getSettings().getString("customizablestarsystems", "opt_glowTexture");
    public final String OPT_ICON_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_iconColor");
    public final String OPT_PITCH = Global.getSettings().getString("customizablestarsystems", "opt_pitch");
    public final String OPT_PLANET_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_planetColor");
    public final String OPT_ROTATION = Global.getSettings().getString("customizablestarsystems", "opt_rotation");
    public final String OPT_TEXTURE = Global.getSettings().getString("customizablestarsystems", "opt_texture");
    public final String OPT_TILT = Global.getSettings().getString("customizablestarsystems", "opt_tilt");
    public final String OPT_USE_REVERSE_LIGHT_FOR_GLOW = Global.getSettings().getString("customizablestarsystems", "opt_useReverseLightForGlow");
    public final String OPT_TYPE_OVERRIDE = Global.getSettings().getString("customizablestarsystems", "opt_typeOverride");

    // Sub-options for certain system features
    public final String OPT_IS_DAMAGED = Global.getSettings().getString("customizablestarsystems", "opt_isDamaged");
    public final String OPT_SIZE = Global.getSettings().getString("customizablestarsystems", "opt_size");
    public final String OPT_INNER_BAND_INDEX = Global.getSettings().getString("customizablestarsystems", "opt_innerBandIndex");
    public final String OPT_OUTER_BAND_INDEX = Global.getSettings().getString("customizablestarsystems", "opt_outerBandIndex");
    public final String OPT_BAND_INDEX = Global.getSettings().getString("customizablestarsystems", "opt_bandIndex");
    public final String OPT_MIDDLE_RADIUS = Global.getSettings().getString("customizablestarsystems", "opt_middleRadius");
    public final String OPT_OUTER_RADIUS = Global.getSettings().getString("customizablestarsystems", "opt_outerRadius");
    public final String OPT_BASE_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_baseColor");
    public final String OPT_AURORA_FREQUENCY = Global.getSettings().getString("customizablestarsystems", "opt_auroraFrequency");
    public final String OPT_AURORA_COLOR = Global.getSettings().getString("customizablestarsystems", "opt_auroraColor");

    // Default types/paths
    public final String DEFAULT_STAR_TYPE = Global.getSettings().getString("customizablestarsystems", "default_star_type");
    public final String DEFAULT_PLANET_TYPE = Global.getSettings().getString("customizablestarsystems", "default_planet_type");
    public final String DEFAULT_FACTION_TYPE = Global.getSettings().getString("customizablestarsystems", "default_faction_type");
    public final String DEFAULT_FRINGE_JUMP_POINT_NAME = Global.getSettings().getString("customizablestarsystems", "default_fringe_jump_point_name");
    public final String DEFAULT_ACCRETION_DISK_NAME = Global.getSettings().getString("customizablestarsystems", "default_accretion_disk_name");
    public final String DEFAULT_CRYOSLEEPER_NAME = Global.getSettings().getString("customizablestarsystems", "default_cryosleeper_name");
    public final String DEFAULT_STATION_TYPE = Global.getSettings().getString("customizablestarsystems", "default_station_type");
    public final String PATH_GRAPHICS_PLANET = Global.getSettings().getString("customizablestarsystems", "path_graphics_planet");
    public final String PATH_GRAPHICS_BACKGROUND = Global.getSettings().getString("customizablestarsystems", "path_graphics_background");
    public final String TYPE_RANDOM_STAR_GIANT = Global.getSettings().getString("customizablestarsystems", "type_random_star_giant");

    // Names used in Solar Array generation
    public final String NAME_MIRROR1 = Global.getSettings().getString("customizablestarsystems", "name_mirror1");
    public final String NAME_MIRROR2 = Global.getSettings().getString("customizablestarsystems", "name_mirror2");
    public final String NAME_MIRROR3 = Global.getSettings().getString("customizablestarsystems", "name_mirror3");
    public final String NAME_MIRROR4 = Global.getSettings().getString("customizablestarsystems", "name_mirror4");
    public final String NAME_MIRROR5 = Global.getSettings().getString("customizablestarsystems", "name_mirror5");
    public final String NAME_SHADE1 = Global.getSettings().getString("customizablestarsystems", "name_shade1");
    public final String NAME_SHADE2 = Global.getSettings().getString("customizablestarsystems", "name_shade2");
    public final String NAME_SHADE3 = Global.getSettings().getString("customizablestarsystems", "name_shade3");

    // Other, used in code
    public final String SOURCE_GEN = Global.getSettings().getString("customizablestarsystems", "source_gen");
    public final String CATEGORY_MISC = Global.getSettings().getString("customizablestarsystems", "category_misc");

    // Error messages
    public final String ERROR_NO_CENTER_STAR = Global.getSettings().getString("customizablestarsystems", "error_noCenterStar");
    public final String ERROR_STAR_TYPE_NOT_FOUND = Global.getSettings().getString("customizablestarsystems", "error_starTypeNotFound");
    public final String ERROR_PLANET_TYPE_NOT_FOUND = Global.getSettings().getString("customizablestarsystems", "error_planetTypeNotFound");
    public final String ERROR_INVALID_FOCUS = Global.getSettings().getString("customizablestarsystems", "error_invalidFocus");
    public final String ERROR_INVALID_CONDITION_UNINHABITED = Global.getSettings().getString("customizablestarsystems", "error_invalidConditionUninhabited");
    public final String ERROR_INVALID_CONDITION_INHABITED = Global.getSettings().getString("customizablestarsystems", "error_invalidConditionInhabited");
    public final String ERROR_INVALID_INDUSTRY = Global.getSettings().getString("customizablestarsystems", "error_invalidIndustry");
    public final String ERROR_INVALID_SALVAGE_ENTITY = Global.getSettings().getString("customizablestarsystems", "error_invalidSalvageEntity");

    // Entities, used in generateSystemFeatures()'s switch cases
    public final String ENTITY_REMNANT_STATION = "remnant_station";
    public final String ENTITY_STATION = "station";
    public final String ENTITY_JUMP_POINT = "jump_point";
    public final String ENTITY_RINGS_ICE = "rings_ice0";
    public final String ENTITY_RINGS_DUST = "rings_dust0";
    public final String ENTITY_RINGS_SPECIAL = "rings_special0";
    public final String ENTITY_RINGS_ASTEROIDS = "rings_asteroids0";

    // Dev/internal ids
    public final String ID_SYSTEM = "system_"; // Most system entity ids should start with this
    public final String ID_STAR = ":star_";
    public final String ID_PLANET = ":planet_";
    public final String ID_STATION = ":station_";
    public final String ID_MARKET = "_market";
    public final String CONDITION_POPULATION = "population_"; // addMarket() appends a number to this

    // Is updated in the addMarket private helper method
    public transient HashMap<MarketAPI, String> marketsToOverrideAdmin;

    // List of proc-gen constellations, filled in during the first setLocation() call
    // Is an ArrayList to more easily get constellations by index
    private transient ArrayList<Constellation> procGenConstellations;

    // List of all vanilla star giants, for "random_star_giant" type
    private final String[] STAR_GIANT_TYPES = {StarTypes.ORANGE_GIANT, StarTypes.RED_GIANT, StarTypes.RED_SUPERGIANT, StarTypes.BLUE_GIANT, StarTypes.BLUE_SUPERGIANT};

    private final Vector2f CORE_WORLD_CENTER = new Vector2f(-6000, -6000);

    // Making a utility class instantiable just so I can modify admins properly D:
    public CSSUtil() {
        marketsToOverrideAdmin = new HashMap<>();
    }

    // Self-note for using opt(): Replace with isNull() in 'if' statement when the default parameter can consume or modify unique entries (e.g. proc-gen names and Random.next())

    /**
     * Generates a customized star systems, as per JSON specifications
     *
     * @param systemOptions Star system options
     * @throws JSONException if systemOptions is invalide
     */
    public void generateCustomStarSystem(JSONObject systemOptions) throws JSONException {
        // Create the star system
        StarSystemAPI system = generateStarSystem(systemOptions);

        // Generate the center stars
        List<PlanetAPI> starsInSystem = generateSystemCenter(system, systemOptions.getJSONObject(OPT_STARS_IN_SYSTEM_CENTER));
        int numOfCenterStars = starsInSystem.size();

        // Create the fringe Jump-point and save its orbit radius
        float fringeRadius = generateFringeJumpPoint(system, systemOptions.optJSONObject(OPT_FRINGE_JUMP_POINT));

        // Create orbiting bodies
        JSONArray planetList = systemOptions.optJSONArray(OPT_ORBITING_BODIES);
        boolean hasFactionPresence = false;
        if (planetList != null) for (int i = 0; i < planetList.length(); i++) {
            PlanetAPI newBody = generateOrbitingBody(system, planetList.getJSONObject(i), numOfCenterStars, i);
            if (newBody.isStar()) starsInSystem.add(newBody);
            if (!hasFactionPresence && !newBody.getFaction().getId().equals(DEFAULT_FACTION_TYPE))
                hasFactionPresence = true;
        }

        // Add the system features
        JSONArray systemFeatures = systemOptions.optJSONArray(OPT_SYSTEM_FEATURES);
        if (systemFeatures != null) for (int i = 0; i < systemFeatures.length(); i++)
            generateSystemFeature(system, systemFeatures.getJSONObject(i), numOfCenterStars);

        // Adds a coronal hypershunt if enabled
        if (systemOptions.optBoolean(OPT_ADD_CORONAL_HYPERSHUNT, false))
            generateHypershunt(system, !hasFactionPresence, true);

        // Adds a Domain-era cryosleeper if enabled
        if (systemOptions.optBoolean(OPT_ADD_DOMAIN_CRYOSLEEPER, false))
            generateCryosleeper(system, DEFAULT_CRYOSLEEPER_NAME, fringeRadius + 4000f, !hasFactionPresence);

        // Add relevant system tags it is NOT a Core World system
        if (!systemOptions.optBoolean(OPT_IS_CORE_WORLD_SYSTEM, false)) {
            system.removeTag(Tags.THEME_CORE);
            system.addTag(Tags.THEME_MISC);
            system.addTag(Tags.THEME_INTERESTING_MINOR);
        }

        // Set the appropriate system music, if applicable
        if (!systemOptions.isNull(OPT_SYSTEM_MUSIC))
            system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, systemOptions.getString(OPT_SYSTEM_MUSIC));

        // Set location of star system either in a constellation or a specified location
        JSONArray locationOverride = systemOptions.optJSONArray(OPT_SET_LOCATION_OVERRIDE);
        if (locationOverride == null)
            setLocation(system, (fringeRadius / 10f) + 100f, systemOptions.optInt(OPT_SET_LOCATION, 0));
        else setLocation(system, locationOverride.getInt(0), locationOverride.getInt(1));

        setSystemType(system);
        setLightColor(system, starsInSystem);

        // Set presence (or lack) of a system-wide nebula
        if (systemOptions.optBoolean(OPT_HAS_SYSTEMWIDE_NEBULA, false))
            StarSystemGenerator.addSystemwideNebula(system, system.getAge());

        // Set the appropriate background, if applicable
        if (!systemOptions.isNull(OPT_SYSTEM_BACKGROUND))
            system.setBackgroundTextureFilename(PATH_GRAPHICS_BACKGROUND + systemOptions.getString(OPT_SYSTEM_BACKGROUND));
        else {
            String nebulaType = system.hasSystemwideNebula() ? StarSystemGenerator.nebulaTypes.get(system.getAge()) : StarSystemGenerator.NEBULA_NONE;
            system.setBackgroundTextureFilename(StarSystemGenerator.backgroundsByNebulaType.get(nebulaType).pick());
        }

        generateHyperspace(system);
        addRemnantWarningBeacons(system);
    }

    /**
     * Sets a star system's location to a specified location
     *
     * @param system Star system to relocate
     * @param x      X-coordinate of the new location
     * @param y      Y-coordinate of the new location
     */
    public void setLocation(StarSystemAPI system, int x, int y) {
        system.getLocation().set(x, y);
        system.setAge(StarAge.AVERAGE); // Can't be ANY, since game will crash if addSystemwideNebula() is ran
    }

    /**
     * Generates a star system
     *
     * @param systemOptions Star system options
     * @return The newly-created star system
     * @throws JSONException if systemOptions is invalid
     */
    public StarSystemAPI generateStarSystem(JSONObject systemOptions) throws JSONException {
        JSONArray starsList = systemOptions.getJSONObject(OPT_STARS_IN_SYSTEM_CENTER).getJSONArray(OPT_STARS);
        if (starsList.length() == 0) throw new IllegalArgumentException(ERROR_NO_CENTER_STAR);
        return Global.getSector().createStarSystem(starsList.getJSONObject(0).isNull(OPT_NAME) ? getProcGenName(Tags.STAR, null) : starsList.getJSONObject(0).getString(OPT_NAME));
    }

    /**
     * Generate a star system center with stars
     *
     * @param system        The star system to modify
     * @param centerOptions Center options
     * @return A List of the newly-created stars
     * @throws JSONException if centerOptions is invlaid
     */
    public List<PlanetAPI> generateSystemCenter(StarSystemAPI system, JSONObject centerOptions) throws JSONException {
        JSONArray starsList = centerOptions.getJSONArray(OPT_STARS);
        int numOfCenterStars = starsList.length();
        String id = Misc.genUID();

        ArrayList<PlanetAPI> stars = new ArrayList<>(numOfCenterStars);
        if (numOfCenterStars == 1) {
            PlanetAPI newStar = addStar(system, starsList.getJSONObject(0), ID_SYSTEM + id);
            if (newStar.getTypeId().equals(StarTypes.BLACK_HOLE)) addAccretionDisk(system, newStar);
            system.setCenter(newStar);
            stars.add(newStar);
        } else {
            SectorEntityToken systemCenter = system.initNonStarCenter(); // Center in which the stars will orbit
            systemCenter.setId(id); // Set the center's id to the unique id

            float orbitRadius = centerOptions.optInt(OPT_ORBIT_RADIUS, 2000) - numOfCenterStars + 1;
            float angle = centerOptions.isNull(OPT_ORBIT_ANGLE) ? StarSystemGenerator.random.nextFloat() * 360f : centerOptions.getInt(OPT_ORBIT_ANGLE);
            float angleDifference = 360f / numOfCenterStars;
            float orbitDays = centerOptions.isNull(OPT_ORBIT_DAYS) ? orbitRadius / ((60f / numOfCenterStars) + StarSystemGenerator.random.nextFloat() * 50f) : centerOptions.getInt(OPT_ORBIT_DAYS);
            char idChar = 'b';

            for (int i = 0; i < numOfCenterStars; i++) {
                PlanetAPI newStar = addStar(system, starsList.getJSONObject(i), ID_SYSTEM + id + (i > 0 ? "_" + idChar++ : ""));
                newStar.setCircularOrbit(systemCenter, angle, orbitRadius + i, orbitDays);
                stars.add(newStar);

                angle = (angle + angleDifference) % 360f;
            }
        }

        return stars;
    }

    /**
     * Generates an orbiting body in a star system
     *
     * @param system           The star system to modify
     * @param bodyOptions      Body options
     * @param numOfCenterStars Number of stars in the star system's center
     * @param index            Index of the body
     * @return The newly-created planet or star
     * @throws JSONException if bodyOptions is invalid
     */
    public PlanetAPI generateOrbitingBody(StarSystemAPI system, JSONObject bodyOptions, int numOfCenterStars, int index) throws JSONException {
        int indexFocus = bodyOptions.optInt(OPT_FOCUS, DEFAULT_FOCUS);
        if (numOfCenterStars + indexFocus > system.getPlanets().size())
            throw new IllegalArgumentException(String.format(ERROR_INVALID_FOCUS, system.getBaseName(), index + 1));

        String systemId = system.getCenter().getId();
        if (!systemId.startsWith(ID_SYSTEM)) systemId = ID_SYSTEM + systemId;

        PlanetAPI newBody = Global.getSettings().getSpec(StarGenDataSpec.class, bodyOptions.optString(OPT_TYPE, null), true) != null ? addStar(system, bodyOptions, systemId + ID_STAR + index) : addPlanet(system, bodyOptions, systemId + ID_PLANET + index);
        addCircularOrbit(newBody, (indexFocus <= 0) ? system.getCenter() : system.getPlanets().get(numOfCenterStars + indexFocus - 1), bodyOptions, 20f);
        if (newBody.hasCondition(Conditions.SOLAR_ARRAY)) addSolarArray(newBody, newBody.getFaction().getId());

        // Adds any entities to this planet's lagrange points if applicable
        JSONArray lagrangePoints = bodyOptions.optJSONArray(OPT_ENTITIES_AT_STABLE_POINTS);
        if (lagrangePoints != null) addToLagrangePoints(newBody, lagrangePoints);

        return newBody;
    }

    /**
     * Adds a star in a star system
     *
     * @param system      The star system to modify
     * @param starOptions Star options
     * @param id          Internal id of the star
     * @return The newly-created star
     * @throws JSONException if starOptions is invalid
     */
    public PlanetAPI addStar(StarSystemAPI system, JSONObject starOptions, String id) throws JSONException {
        String starType = starOptions.optString(OPT_TYPE, DEFAULT_STAR_TYPE);
        if (starType.equals(TYPE_RANDOM_STAR_GIANT))
            starType = STAR_GIANT_TYPES[StarSystemGenerator.random.nextInt(STAR_GIANT_TYPES.length)];

        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, starType, true);
        if (starData == null) throw new IllegalArgumentException(String.format(ERROR_STAR_TYPE_NOT_FOUND, starType));

        float radius = starOptions.optInt(OPT_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (radius <= 0)
            radius = starData.getMinRadius() + (starData.getMaxRadius() - starData.getMinRadius()) * StarSystemGenerator.random.nextFloat();

        float coronaRadius = starOptions.optInt(OPT_CORONA_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (coronaRadius <= 0)
            coronaRadius = Math.max(starData.getCoronaMin(), radius * (starData.getCoronaMult() + starData.getCoronaVar() * (StarSystemGenerator.random.nextFloat() - 0.5f)));

        float flareChance = (float) starOptions.optDouble(OPT_FLARE_CHANCE, DEFAULT_SET_TO_PROC_GEN);
        if (flareChance < 0)
            flareChance = starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * StarSystemGenerator.random.nextFloat();

        PlanetAPI newStar;
        if (system.getStar() == null) { // First star in system, so initialize system star
            newStar = system.initStar(id, starType, radius, coronaRadius, starData.getSolarWind(), flareChance, starData.getCrLossMult());
        } else { // Add another star in the system; will have to set appropriate system type elsewhere depending if it will be on center or orbiting the center
            String name = starOptions.optString(OPT_NAME, null);
            if (name == null) name = getProcGenName(Tags.STAR, system.getBaseName());

            // Need to set a default orbit, else new game creation will fail when attempting to save
            newStar = system.addPlanet(id, system.getCenter(), name, starType, 0f, radius, 10000f, 1000f);
            system.addCorona(newStar, coronaRadius, starData.getSolarWind(), flareChance, starData.getCrLossMult());
        }

        // Add special star hazards if applicable
        if (starType.equals(StarTypes.BLACK_HOLE) || starType.equals(StarTypes.NEUTRON_STAR)) {
            StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(newStar);
            if (coronaPlugin != null) system.removeEntity(coronaPlugin.getEntity());


            String coronaType = starType.equals(StarTypes.BLACK_HOLE) ? Terrain.EVENT_HORIZON : Terrain.PULSAR_BEAM;
            if (coronaType.equals(Terrain.PULSAR_BEAM)) system.addCorona(newStar, 300, 3, 0, 3);
            system.addTerrain(coronaType, new StarCoronaTerrainPlugin.CoronaParams(newStar.getRadius() + coronaRadius, (newStar.getRadius() + coronaRadius) / 2f, newStar, starData.getSolarWind(), flareChance, starData.getCrLossMult())).setCircularOrbit(newStar, 0, 0, 100);
        }

        // Apply any spec changes
        addSpecChanges(newStar, starOptions.optJSONObject(OPT_SPEC_CHANGES));

        addCustomDescription(newStar, starOptions);

        return newStar;
    }

    /**
     * Adds a planet in a star system
     *
     * @param system        The star system to modify
     * @param planetOptions Planet options
     * @param id            Internal id of the planet
     * @return The newly-created planet
     * @throws JSONException if planetOptions is invalid
     */
    public PlanetAPI addPlanet(StarSystemAPI system, JSONObject planetOptions, String id) throws JSONException {
        String planetType = planetOptions.optString(OPT_TYPE, DEFAULT_PLANET_TYPE);
        PlanetGenDataSpec planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planetType, true);
        if (planetData == null)
            throw new IllegalArgumentException(String.format(ERROR_PLANET_TYPE_NOT_FOUND, planetType));

        String name = planetOptions.optString(OPT_NAME, null);
        if (name == null) name = getProcGenName(Tags.PLANET, system.getBaseName());

        float radius = planetOptions.optInt(OPT_RADIUS, DEFAULT_SET_TO_PROC_GEN);
        if (radius <= 0)
            radius = planetData.getMinRadius() + (planetData.getMaxRadius() - planetData.getMinRadius()) * StarSystemGenerator.random.nextFloat();

        // Need to set a default orbit, else new game creation will fail when attempting to save
        PlanetAPI newPlanet = system.addPlanet(id, system.getCenter(), name, planetType, 0f, radius, 10000f, 1000f);
        newPlanet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, StarSystemGenerator.random.nextLong());

        // Apply any spec changes
        addSpecChanges(newPlanet, planetOptions.optJSONObject(OPT_SPEC_CHANGES));

        addCustomDescription(newPlanet, planetOptions);

        int marketSize = planetOptions.optInt(OPT_MARKET_SIZE, DEFAULT_MARKET_SIZE);
        if (marketSize <= 0) setPlanetConditions(newPlanet, planetOptions);
        else addMarket(newPlanet, planetOptions, marketSize);

        return newPlanet;
    }

    // Adds a custom description to an entity, if applicable
    private void addCustomDescription(SectorEntityToken entity, JSONObject options) {
        String id = options.optString(OPT_CUSTOM_DESCRIPTION_ID, null);
        if (id != null) entity.setCustomDescriptionId(id);
    }

    // Adds spec changes to a body
    // Note: Textures must already be preloaded via settings.json for texture-replacing fields to work
    // Partially adapted from Tartiflette's Unknown Skies source code
    private void addSpecChanges(PlanetAPI body, JSONObject specChanges) throws JSONException {
        if (specChanges == null) return;
        PlanetSpecAPI bodySpec = body.getSpec();

        JSONArray atmosphereColor = specChanges.optJSONArray(OPT_ATMOSPHERE_COLOR);
        if (atmosphereColor != null) bodySpec.setAtmosphereColor(getColor(atmosphereColor));

        float atmosphereThickness = (float) specChanges.optDouble(OPT_ATMOSPHERE_THICKNESS);
        if (!Float.isNaN(atmosphereThickness)) bodySpec.setAtmosphereThickness(atmosphereThickness);

        float atmosphereThicknessMin = (float) specChanges.optDouble(OPT_ATMOSPHERE_THICKNESS_MIN);
        if (!Float.isNaN(atmosphereThicknessMin)) bodySpec.setAtmosphereThicknessMin(atmosphereThicknessMin);

        JSONArray cloudColor = specChanges.optJSONArray(OPT_CLOUD_COLOR);
        if (cloudColor != null) bodySpec.setCloudColor(getColor(cloudColor));

        float cloudRotation = (float) specChanges.optDouble(OPT_CLOUD_ROTATION);
        if (!Float.isNaN(cloudRotation)) bodySpec.setCloudRotation(cloudRotation);

        String cloudTexture = specChanges.optString(OPT_CLOUD_TEXTURE, null);
        if (cloudTexture != null) bodySpec.setCloudTexture(PATH_GRAPHICS_PLANET + cloudTexture);

        JSONArray glowColor = specChanges.optJSONArray(OPT_GLOW_COLOR);
        if (glowColor != null) bodySpec.setGlowColor(getColor(glowColor));

        String glowTexture = specChanges.optString(OPT_GLOW_TEXTURE, null);
        if (glowTexture != null) bodySpec.setGlowTexture(PATH_GRAPHICS_PLANET + glowTexture);

        JSONArray iconColor = specChanges.optJSONArray(OPT_ICON_COLOR);
        if (iconColor != null) bodySpec.setIconColor(getColor(iconColor));

        float pitch = (float) specChanges.optDouble(OPT_PITCH);
        if (!Float.isNaN(pitch)) bodySpec.setPitch(pitch);

        JSONArray planetColor = specChanges.optJSONArray(OPT_PLANET_COLOR);
        if (planetColor != null) bodySpec.setPlanetColor(getColor(planetColor));

        float rotation = (float) specChanges.optDouble(OPT_ROTATION);
        if (!Float.isNaN(rotation)) bodySpec.setRotation(rotation);

        String texture = specChanges.optString(OPT_TEXTURE, null);
        if (texture != null) bodySpec.setTexture(PATH_GRAPHICS_PLANET + texture);

        float tilt = (float) specChanges.optDouble(OPT_TILT);
        if (!Float.isNaN(tilt)) bodySpec.setTilt(tilt);

        boolean revLightGlow = specChanges.optBoolean(OPT_USE_REVERSE_LIGHT_FOR_GLOW);
        if (revLightGlow != bodySpec.isUseReverseLightForGlow()) bodySpec.setUseReverseLightForGlow(revLightGlow);

        JSONArray typeOverride = specChanges.optJSONArray(OPT_TYPE_OVERRIDE);
        if (typeOverride != null) {
            String newType = typeOverride.getString(0);
            ((PlanetSpec) bodySpec).planetType = newType;
            ((PlanetSpec) bodySpec).name = typeOverride.getString(1);
            ((PlanetSpec) bodySpec).descriptionId = newType;
            body.setTypeId(newType);
        }

        body.applySpecChanges();
    }

    /**
     * Adds system features to a planet's lagrange points; attempts to add a custom entity if handling unsupported types
     *
     * @param planet         The planet to modify
     * @param lagrangePoints List of JSONObjects representing system features
     * @throws JSONException If lagrangePoints is invalid
     */
    public void addToLagrangePoints(PlanetAPI planet, JSONArray lagrangePoints) throws JSONException {
        JSONArray lagrangePoint3 = lagrangePoints.optJSONArray(0);
        if (lagrangePoint3 == null) addLagrangePointFeature(planet, lagrangePoints.optJSONObject(0), 3);
        else for (int i = 0; i < lagrangePoint3.length(); i++)
            addLagrangePointFeature(planet, lagrangePoint3.optJSONObject(i), 3);

        JSONArray lagrangePoint4 = lagrangePoints.optJSONArray(1);
        if (lagrangePoint4 == null) addLagrangePointFeature(planet, lagrangePoints.optJSONObject(1), 4);
        else for (int i = 0; i < lagrangePoint4.length(); i++)
            addLagrangePointFeature(planet, lagrangePoint4.optJSONObject(i), 4);

        JSONArray lagrangePoint5 = lagrangePoints.optJSONArray(2);
        if (lagrangePoint5 == null) addLagrangePointFeature(planet, lagrangePoints.optJSONObject(2), 5);
        else for (int i = 0; i < lagrangePoint5.length(); i++)
            addLagrangePointFeature(planet, lagrangePoint5.optJSONObject(i), 5);
    }

    // Adds a system feature to a specific lagrange point of a planet
    private void addLagrangePointFeature(PlanetAPI planet, JSONObject featureOptions, int lagrangePoint) throws JSONException {
        if (featureOptions == null) return;

        String type = featureOptions.optString(OPT_TYPE, null);
        if (type == null) return; // Lagrange point should remain empty
        float lagrangeAngle = planet.getCircularOrbitAngle();
        switch (lagrangePoint) {
            case 3: // L3 point
                lagrangeAngle -= 180f;
                break;
            case 4: // L4 point
                lagrangeAngle += 60f;
                break;
            case 5: // L5 point
                lagrangeAngle -= 60f;
        }

        SectorEntityToken entity;
        StarSystemAPI system = planet.getStarSystem();
        switch (type) {
            case Tags.PLANET:
                entity = addPlanet(system, featureOptions.getJSONObject(OPT_PLANET_OPTIONS), planet.getId() + ID_PLANET + lagrangePoint);
                break;
            case Terrain.ASTEROID_FIELD:
                entity = addAsteroidField(system, featureOptions);
                break;
            case ENTITY_REMNANT_STATION:
                entity = addRemnantStation(system, featureOptions);
                break;
            case ENTITY_STATION:
                entity = addStation(system, featureOptions);
                break;
            case ENTITY_JUMP_POINT:
                entity = addJumpPoint(system, featureOptions);
                break;
            case Entities.COMM_RELAY:
            case Entities.COMM_RELAY_MAKESHIFT:
            case Entities.NAV_BUOY:
            case Entities.NAV_BUOY_MAKESHIFT:
            case Entities.SENSOR_ARRAY:
            case Entities.SENSOR_ARRAY_MAKESHIFT:
                entity = addObjective(system, featureOptions, type);
                break;
            // These usually don't have a set faction, but whatever
            case Entities.INACTIVE_GATE:
            case Entities.STABLE_LOCATION:
            default: // Default option in case of mods adding their own system entities
                entity = addCustomEntity(system, featureOptions, type);
        }

        if (type.equals(Tags.PLANET)) {
            entity.setCircularOrbit(planet.getOrbitFocus(), lagrangeAngle, planet.getCircularOrbitRadius(), planet.getCircularOrbitPeriod());
        } else {
            entity.setCircularOrbitPointingDown(planet.getOrbitFocus(), lagrangeAngle, planet.getCircularOrbitRadius(), planet.getCircularOrbitPeriod());
        }
    }

    /**
     * Generates an orbiting system feature in a star system
     *
     * @param system           The star system to modify
     * @param featureOptions   Feature options
     * @param numOfCenterStars Numbers of stars in star system's center
     * @throws JSONException if featureOptions is invalid
     */
    public void generateSystemFeature(StarSystemAPI system, JSONObject featureOptions, int numOfCenterStars) throws JSONException {
        int focusIndex = numOfCenterStars + (featureOptions.optInt(OPT_FOCUS, DEFAULT_FOCUS));
        SectorEntityToken focus = (focusIndex == numOfCenterStars) ? system.getCenter() : system.getPlanets().get(focusIndex - 1);
        String type = featureOptions.getString(OPT_TYPE);

        boolean alreadyGenerated = true;
        switch (type) { // For whole-orbit entities
            case Tags.ACCRETION_DISK:
                addAccretionDisk(system, focus);
                break;
            case Terrain.MAGNETIC_FIELD:
                addMagneticField(system, focus, featureOptions);
                break;
            case ENTITY_RINGS_ICE:
            case ENTITY_RINGS_DUST:
            case ENTITY_RINGS_SPECIAL:
            case ENTITY_RINGS_ASTEROIDS:
                addRingBand(system, focus, featureOptions, type);
                break;
            case Terrain.ASTEROID_BELT:
                addAsteroidBelt(system, focus, featureOptions);
                break;
            default:
                alreadyGenerated = false;
        }

        if (alreadyGenerated) return;

        SectorEntityToken entity;
        switch (type) {
            case Terrain.ASTEROID_FIELD:
                entity = addAsteroidField(system, featureOptions);
                addCircularOrbit(entity, focus, featureOptions, 20f);
                break;
            case ENTITY_REMNANT_STATION:
                entity = addRemnantStation(system, featureOptions);
                addCircularOrbitWithSpin(entity, focus, featureOptions, 20f, 5f, 5f);
                break;
            case ENTITY_STATION:
                entity = addStation(system, featureOptions);
                addCircularOrbitPointingDown(entity, focus, featureOptions, 20f);
                break;
            case Entities.INACTIVE_GATE:
                entity = addCustomEntity(system, featureOptions, type);
                addCircularOrbit(entity, focus, featureOptions, 10f);
                break;
            case Entities.STABLE_LOCATION:
                entity = addCustomEntity(system, featureOptions, type);
                addCircularOrbitWithSpin(entity, focus, featureOptions, 20f, 1f, 11f);
                break;
            case ENTITY_JUMP_POINT:
                entity = addJumpPoint(system, featureOptions);
                addCircularOrbit(entity, focus, featureOptions, 15f);
                break;
            case Entities.COMM_RELAY:
            case Entities.COMM_RELAY_MAKESHIFT:
            case Entities.NAV_BUOY:
            case Entities.NAV_BUOY_MAKESHIFT:
            case Entities.SENSOR_ARRAY:
            case Entities.SENSOR_ARRAY_MAKESHIFT:
                entity = addObjective(system, featureOptions, type);
                addCircularOrbitWithSpin(entity, focus, featureOptions, 20f, 1f, 11f);
                break;
            default: // Any salvage entities defined in salvage_entity_gen_data.csv (including ones added by mods)
                entity = addSalvageEntity(system, featureOptions, type);
                if (type.equals(Entities.CORONAL_TAP)) addCircularOrbitPointingDown(entity, focus, featureOptions, 15f);
                else addCircularOrbitWithSpin(entity, focus, featureOptions, 20f, 1f, 11f);
        }
    }

    public void addCircularOrbit(SectorEntityToken entity, SectorEntityToken focus, JSONObject entityOptions, float defaultOrbitDaysDivisor) throws JSONException {
        float orbitRadius = entityOptions.getInt(OPT_ORBIT_RADIUS);

        float angle = entityOptions.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
        if (angle < 0) angle = StarSystemGenerator.random.nextFloat() * 360f;

        float orbitDays = entityOptions.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0)
            orbitDays = orbitRadius / (defaultOrbitDaysDivisor + StarSystemGenerator.random.nextFloat() * 5f);

        entity.setCircularOrbit(focus, angle, orbitRadius, orbitDays);
    }

    public void addCircularOrbitPointingDown(SectorEntityToken entity, SectorEntityToken focus, JSONObject entityOptions, float defaultOrbitDaysDivisor) throws JSONException {
        float orbitRadius = entityOptions.getInt(OPT_ORBIT_RADIUS);

        float angle = entityOptions.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
        if (angle < 0) angle = StarSystemGenerator.random.nextFloat() * 360f;

        float orbitDays = entityOptions.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0)
            orbitDays = orbitRadius / (defaultOrbitDaysDivisor + StarSystemGenerator.random.nextFloat() * 5f);

        entity.setCircularOrbitPointingDown(focus, angle, orbitRadius, orbitDays);
    }

    public void addCircularOrbitWithSpin(SectorEntityToken entity, SectorEntityToken focus, JSONObject entityOptions, float defaultOrbitDaysDivisor, float minSpin, float maxSpin) throws JSONException {
        float orbitRadius = entityOptions.getInt(OPT_ORBIT_RADIUS);

        float angle = entityOptions.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
        if (angle < 0) angle = StarSystemGenerator.random.nextFloat() * 360f;

        float orbitDays = entityOptions.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
        if (orbitDays <= 0)
            orbitDays = orbitRadius / (defaultOrbitDaysDivisor + StarSystemGenerator.random.nextFloat() * 5f);

        entity.setCircularOrbitWithSpin(focus, angle, orbitRadius, orbitDays, minSpin, maxSpin);
    }

    /**
     * Generates a custom entity in a star system
     *
     * @param system  The star system to modify
     * @param options Entity options
     * @param type    Type of entity
     * @return The newly-created entity
     */
    public SectorEntityToken addCustomEntity(StarSystemAPI system, JSONObject options, String type) {
        SectorEntityToken entity = system.addCustomEntity(null, options.optString(OPT_NAME, null), type, options.optString(OPT_FACTION_ID, null));
        addCustomDescription(entity, options);
        return entity;
    }

    /**
     * Adds a station in a system
     *
     * @param system  The system to modify
     * @param options Station options
     * @throws JSONException If stationOptions is invalid
     */
    public SectorEntityToken addStation(StarSystemAPI system, JSONObject options) throws JSONException {
        SectorEntityToken station = system.addCustomEntity(system.getStar().getId() + ID_STATION + Misc.genUID(), options.optString(OPT_NAME, null), options.optString(OPT_STATION_TYPE, DEFAULT_STATION_TYPE), options.optString(OPT_FACTION_ID, null));
        addCustomDescription(station, options);

        int marketSize = options.optInt(OPT_MARKET_SIZE, DEFAULT_MARKET_SIZE);
        if (marketSize <= 0) Misc.setAbandonedStationMarket(station.getId(), station);
        else addMarket(station, options, marketSize);

        return station;
    }

    /**
     * <p>Creates a Remnant battlestation to this system</p>
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator's
     * addBattlestations() for vanilla implementation</p>
     *
     * @param system  The system to modify
     * @param options Station options
     * @return The newly-created station
     */
    public CampaignFleetAPI addRemnantStation(StarSystemAPI system, JSONObject options) {
        boolean isDamaged = options.optBoolean(OPT_IS_DAMAGED, false);

        CampaignFleetAPI station = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, isDamaged ? "remnant_station2_Damaged" : "remnant_station2_Standard");
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

        PersonAPI commander = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE).createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, StarSystemGenerator.random);

        station.setCommander(commander);
        station.getFlagship().setCaptain(commander);

        system.addTag(Tags.THEME_INTERESTING);
        system.addTag(Tags.THEME_REMNANT);
        system.addTag(Tags.THEME_REMNANT_SECONDARY);
        system.addTag(Tags.THEME_UNSAFE);
        if (isDamaged) {
            station.getMemoryWithoutUpdate().set("$damagedStation", true);
            station.setName(station.getName() + " (Damaged)");

            system.addScript(new RemnantStationFleetManager(station, 1f, 0, 2 + StarSystemGenerator.random.nextInt(3), 25f, 6, 12));
            system.addTag(Tags.THEME_REMNANT_SUPPRESSED);
        } else {
            RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(station.getFlagship());
            RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, station, null, 3, StarSystemGenerator.random);

            system.addScript(new RemnantStationFleetManager(station, 1f, 0, 8 + StarSystemGenerator.random.nextInt(5), 15f, 8, 24));
            system.addTag(Tags.THEME_REMNANT_RESURGENT);
        }

        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

        return station;
    }

    /**
     * Adds an asteroid belt around a focus
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.AsteroidBeltGenPlugin's
     * generate() for vanilla implementation</p>
     *
     * @param system  The system to modify
     * @param focus   The focus
     * @param options Asteroid belt options
     */
    public void addAsteroidBelt(StarSystemAPI system, SectorEntityToken focus, JSONObject options) {
        String name = options.optString(OPT_NAME, null);
        float orbitRadius = options.optInt(OPT_ORBIT_RADIUS, Math.round(focus.getRadius()) + 100);
        float width = options.optInt(OPT_SIZE, 256);
        int innerBandIndex = options.optInt(OPT_INNER_BAND_INDEX, 0);
        int outerBandIndex = options.optInt(OPT_OUTER_BAND_INDEX, 0);

        if (innerBandIndex < 0 || innerBandIndex > 3) innerBandIndex = 0;
        if (outerBandIndex < 0 || outerBandIndex > 3) outerBandIndex = 0;
        float orbitDays = orbitRadius / (15f + 5f * StarSystemGenerator.random.nextFloat());
        int count = (int) (orbitDays * (0.25f + 0.5f * StarSystemGenerator.random.nextFloat()));
        if (count > 100) count = (int) (100f + (count - 100f) * 0.25f);
        if (count > 250) count = 250;
        system.addAsteroidBelt(focus, count, orbitRadius, width, orbitDays * .75f, orbitDays * 1.5f, Terrain.ASTEROID_BELT, name);
        system.addRingBand(focus, CATEGORY_MISC, ENTITY_RINGS_ASTEROIDS, 256f, innerBandIndex, Color.white, 256f, orbitRadius - width * 0.25f, orbitDays * 1.05f, null, null);
        system.addRingBand(focus, CATEGORY_MISC, ENTITY_RINGS_ASTEROIDS, 256f, outerBandIndex, Color.white, 256f, orbitRadius + width * 0.25f, orbitDays, null, null);
    }

    /**
     * Adds an asteroid field around a focus
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.AsteroidFieldGenPlugin's
     * generate() for vanilla implementation</p>
     *
     * @param system  The system to modify
     * @param options Asteroid field options
     * @return The newly-created asteroid field
     */
    public SectorEntityToken addAsteroidField(StarSystemAPI system, JSONObject options) {
        String name = options.optString(OPT_NAME, null);
        float radius = options.optInt(OPT_SIZE, 400);
        int count = (int) (radius * radius * 3.14f / 80000f);
        if (count < 10) count = 10;
        if (count > 100) count = 100;
        return system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(radius, radius + 100f, count, count, 4f, 16f, name));
    }

    /**
     * Adds a ring band around a focus
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.RingGenPlugin's
     * generate() for vanilla implementation</p>
     *
     * @param system  The system to modify
     * @param focus   The focus
     * @param options Ring band options
     * @param type    Type of ring band
     */
    public void addRingBand(StarSystemAPI system, SectorEntityToken focus, JSONObject options, String type) {
        String name = options.optString(OPT_NAME, null);
        float orbitRadius = options.optInt(OPT_ORBIT_RADIUS, Math.round(focus.getRadius()) + 100);
        int bandIndex = options.optInt(OPT_BAND_INDEX, 1);

        if (type.equals(ENTITY_RINGS_SPECIAL) ? bandIndex != 1 : (bandIndex < 0 || bandIndex > 3)) bandIndex = 1;
        system.addRingBand(focus, CATEGORY_MISC, type, 256f, bandIndex, Color.white, 256f, orbitRadius, orbitRadius / (15f + 5f * StarSystemGenerator.random.nextFloat()), Terrain.RING, name);
    }

    /**
     * Adds a magnetic field around a focus
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.MagFieldGenPlugin's
     * generate() for vanilla implementation</p>
     *
     * @param system  The star system to modify
     * @param focus   The focus
     * @param options Magnetic field options
     */
    public void addMagneticField(StarSystemAPI system, SectorEntityToken focus, JSONObject options) throws JSONException {
        float width = options.optInt(OPT_SIZE, 300);

        float orbitRadius = options.optInt(OPT_ORBIT_RADIUS, Math.round(focus.getRadius()) + 100); // Inner radius, or visual band start
        float middleRadius = options.optInt(OPT_MIDDLE_RADIUS, (int) (orbitRadius + width / 2f));
        float outerRadius = options.optInt(OPT_OUTER_RADIUS, (int) (orbitRadius + width));

        Color baseColor = getColor(options.optJSONArray(OPT_BASE_COLOR));
        if (baseColor == null) baseColor = new Color(50, 20, 100, 40);

        float auroraFrequency = (float) options.optDouble(OPT_AURORA_FREQUENCY, DEFAULT_SET_TO_PROC_GEN);
        if (auroraFrequency < 0) auroraFrequency = 0.25f + 0.75f * StarSystemGenerator.random.nextFloat();

        Color[] auroraColors = getColors(options.optJSONArray(OPT_AURORA_COLOR));
        if (auroraColors == null)
            auroraColors = new Color[]{new Color(140, 100, 235), new Color(180, 110, 210), new Color(150, 140, 190), new Color(140, 190, 210), new Color(90, 200, 170), new Color(65, 230, 160), new Color(20, 220, 70)};

        system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(width, middleRadius, focus, orbitRadius, outerRadius, baseColor, auroraFrequency, auroraColors)).setCircularOrbit(focus, 0, 0, 100f);
    }

    // Gets a Color from a JSONArray of suitable size
    private Color getColor(JSONArray rgba) throws JSONException {
        if (rgba == null) return null;
        return new Color(rgba.getInt(0), rgba.getInt(1), rgba.getInt(2), rgba.getInt(3));
    }

    // Gets an array of Colors from a JSONArray containing colors
    private Color[] getColors(JSONArray colorList) throws JSONException {
        if (colorList == null) return null;

        Color[] colors = new Color[colorList.length()];
        for (int i = 0; i < colorList.length(); i++) colors[i] = getColor(colorList.getJSONArray(i));

        return colors;
    }

    /**
     * Adds a jump-point in a star system
     *
     * @param system  The star system to modify
     * @param options Jump-point options
     * @return The newly-created jump-point
     */
    public JumpPointAPI addJumpPoint(StarSystemAPI system, JSONObject options) {
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(null, options.optString(OPT_NAME, null));
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        return jumpPoint;
    }

    /**
     * Adds a salvageable entity in a star system
     *
     * @param system  The star system to modify
     * @param options Salvage entity options
     * @param type    Type of salvage entity
     * @return The newly-created entity
     */
    public SectorEntityToken addSalvageEntity(StarSystemAPI system, JSONObject options, String type) {
        SalvageEntityGenDataSpec salvageData = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, type, true);
        if (salvageData == null) throw new IllegalArgumentException(String.format(ERROR_INVALID_SALVAGE_ENTITY, type));

        SectorEntityToken salvageEntity = system.addCustomEntity(null, options.optString(OPT_NAME, null), type, null);
        salvageEntity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, StarSystemGenerator.random.nextLong());
        salvageEntity.setSensorProfile(1f);
        salvageEntity.setDiscoverable(true);
        salvageEntity.getDetectedRangeMod().modifyFlat(SOURCE_GEN, salvageData.getDetectionRange());

        // Set proper attributes for certain entities
        switch (type) {
            case Entities.CORONAL_TAP:
                system.addTag(Tags.HAS_CORONAL_TAP);
                system.addTag(Tags.THEME_INTERESTING);
                break;
            case Entities.DERELICT_CRYOSLEEPER:
                salvageEntity.setFaction(Factions.DERELICT);
                system.addTag(Tags.THEME_DERELICT_CRYOSLEEPER);
                system.addTag(Tags.THEME_INTERESTING);
        }

        addCustomDescription(salvageEntity, options);

        return salvageEntity;
    }

    /**
     * Adds a system objective in a star system
     *
     * @param system      The star system to modify
     * @param options     Objective options
     * @param objectiveId System objective id; should either be "comm_relay", "sensor_array", "nav_buoy", or their makeshift variants
     * @return The newly-created system objective
     */
    public SectorEntityToken addObjective(StarSystemAPI system, JSONObject options, String objectiveId) {
        String factionId = options.optString(OPT_FACTION_ID, null);
        SectorEntityToken objective = system.addCustomEntity(null, options.optString(OPT_NAME, null), objectiveId, factionId);
        if (factionId == null || factionId.equals(Factions.NEUTRAL))
            objective.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
        addCustomDescription(objective, options);

        return objective;
    }

    /**
     * <p>Adds an accretion disk to an entity</p>
     * <p>(Credit to theDragn#0580 for publishing the original code on #advanced-modmaking in the Unofficial Starsector Discord.
     * It was apparently taken from the vanilla implementation in the source API, but I couldn't find exactly where in the API)</p>
     *
     * @param system System to modify
     * @param focus  Entity to modify
     */
    public void addAccretionDisk(StarSystemAPI system, SectorEntityToken focus) {
        Random randomSeed = StarSystemGenerator.random;
        float orbitRadius = Math.max(focus.getRadius(), 90f) * (10f + randomSeed.nextFloat() * 5f);
        float bandWidth = 256f;
        int numBands = 12 + randomSeed.nextInt(7);
        for (int i = 0; i < numBands; i++) {
            float radius = orbitRadius - (i * bandWidth * 0.25f) - (i * bandWidth * 0.1f);
            RingBandAPI visual = system.addRingBand(focus, CATEGORY_MISC, ENTITY_RINGS_ICE, 256f, 0, new Color(46, 35, 173), bandWidth, radius + bandWidth / 2f, -(radius / (30f + 10f * randomSeed.nextFloat())));
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0);
            visual.setSpiralFactor(2f + randomSeed.nextFloat() * 5f);
        }

        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius, orbitRadius / 2f, focus, DEFAULT_ACCRETION_DISK_NAME));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(focus, 0, 0, -100);
    }

    /**
     * Adds a fringe jump-point in a system. Can accept also accept 'null' for options
     *
     * @param system  System to modify
     * @param options Jump-point options
     * @return The orbit radius of the newly-created jump-point
     */
    public float generateFringeJumpPoint(StarSystemAPI system, JSONObject options) {
        String name;
        float orbitRadius;
        float angle;
        float orbitDays;
        if (options != null) {
            name = options.optString(OPT_NAME, DEFAULT_FRINGE_JUMP_POINT_NAME);
            orbitRadius = options.optInt(OPT_ORBIT_RADIUS, DEFAULT_FRINGE_ORBIT_RADIUS);

            angle = options.optInt(OPT_ORBIT_ANGLE, DEFAULT_SET_TO_PROC_GEN);
            if (angle < 0) angle = StarSystemGenerator.random.nextFloat() * 360f;

            orbitDays = options.optInt(OPT_ORBIT_DAYS, DEFAULT_SET_TO_PROC_GEN);
            if (orbitDays <= 0) orbitDays = orbitRadius / (15f + StarSystemGenerator.random.nextFloat() * 5f);
        } else {
            name = DEFAULT_FRINGE_JUMP_POINT_NAME;
            orbitRadius = DEFAULT_FRINGE_ORBIT_RADIUS;
            angle = StarSystemGenerator.random.nextFloat() * 360f;
            orbitDays = DEFAULT_FRINGE_ORBIT_RADIUS / (15f + StarSystemGenerator.random.nextFloat() * 5f);
        }

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(null, name);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        jumpPoint.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);

        return orbitRadius;
    }

    /**
     * <p>Adds a Domain-era cryosleeper in a star system</p>
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator's
     * addCryosleeper() for vanilla implementation</p>
     *
     * @param system       Star system to modify
     * @param name         Name of the cryosleeper
     * @param orbitRadius  How far cryosleeper is located from center of system
     * @param discoverable Whether cryosleeper needs to be discovered before being revealed in map
     */
    public void generateCryosleeper(StarSystemAPI system, String name, float orbitRadius, boolean discoverable) {
        Random randomSeed = StarSystemGenerator.random;
        SectorEntityToken cryosleeper = system.addCustomEntity(null, name, Entities.DERELICT_CRYOSLEEPER, Factions.DERELICT);
        cryosleeper.setCircularOrbitWithSpin(system.getCenter(), randomSeed.nextFloat() * 360f, orbitRadius, orbitRadius / (15f + randomSeed.nextFloat() * 5f), 1f, 11);
        cryosleeper.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, randomSeed.nextLong());

        if (discoverable) {
            cryosleeper.setSensorProfile(1f);
            cryosleeper.setDiscoverable(true);
            cryosleeper.getDetectedRangeMod().modifyFlat(SOURCE_GEN, 3500f);
        }

        system.addTag(Tags.THEME_DERELICT_CRYOSLEEPER);
        system.addTag(Tags.THEME_INTERESTING);
    }

    /**
     * <p>Adds a coronal hypershunt in a star system.</p>
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator's
     * addCoronalTaps() for vanilla implementation</p>
     *
     * @param system             Star system to modify
     * @param discoverable       Whether hypershunt needs to be discovered before being revealed in map
     * @param hasParticleEffects Whether the hypershunt should emit particle effects upon activation; set to false for better performance
     */
    public void generateHypershunt(StarSystemAPI system, boolean discoverable, boolean hasParticleEffects) {
        SectorEntityToken systemCenter = system.getCenter();
        SectorEntityToken hypershunt = system.addCustomEntity(null, null, Entities.CORONAL_TAP, null);
        if (systemCenter.isStar()) { // Orbit the sole star
            float orbitRadius = systemCenter.getRadius() + hypershunt.getRadius() + 100f;
            hypershunt.setCircularOrbitPointingDown(systemCenter, StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitRadius / 20f);
        } else { // Stay in the center, facing towards the primary star
            PlanetAPI primaryStar = system.getStar();
            hypershunt.setCircularOrbitPointingDown(primaryStar, (primaryStar.getCircularOrbitAngle() - 180f) % 360f, primaryStar.getCircularOrbitRadius(), primaryStar.getCircularOrbitPeriod());
        }

        if (discoverable) {
            hypershunt.setSensorProfile(1f);
            hypershunt.setDiscoverable(true);
            hypershunt.getDetectedRangeMod().modifyFlat(SOURCE_GEN, 3500f);
        }

        if (hasParticleEffects) system.addScript(new CoronalTapParticleScript(hypershunt));

        system.addTag(Tags.HAS_CORONAL_TAP);
        system.addTag(Tags.THEME_INTERESTING);
    }

    // Sets planetary conditions to planet
    private void setPlanetConditions(PlanetAPI planet, JSONObject planetOptions) throws JSONException {
        Misc.initConditionMarket(planet);
        MarketAPI planetMarket = planet.getMarket();
        JSONArray conditions = planetOptions.optJSONArray(OPT_CONDITIONS);
        if (conditions != null) for (int i = 0; i < conditions.length(); i++)
            try {
                planetMarket.addCondition(conditions.getString(i));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_CONDITION_UNINHABITED, conditions.getString(i), planet.getTypeId(), Math.round(planet.getCircularOrbitRadius())));
            }
    }

    // Adds a populated market to a specfied entity
    private void addMarket(SectorEntityToken entity, JSONObject marketOptions, int size) throws JSONException {
        if (size > 10) size = 10;
        // Create market on specified entity
        String factionId = marketOptions.getString(OPT_FACTION_ID);
        MarketAPI planetMarket = Global.getFactory().createMarket(entity.getId() + ID_MARKET, entity.getName(), size);
        planetMarket.setFactionId(factionId);
        planetMarket.setPrimaryEntity(entity);
        planetMarket.getTariff().setBaseValue(0.3f); // Default tariff value
        planetMarket.setFreePort(marketOptions.optBoolean(OPT_FREE_PORT, false));

        // Add conditions
        planetMarket.addCondition(CONDITION_POPULATION + size);
        JSONArray conditions = marketOptions.optJSONArray(OPT_CONDITIONS);
        if (conditions != null) for (int i = 0; i < conditions.length(); i++)
            try {
                planetMarket.addCondition(conditions.getString(i));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_CONDITION_INHABITED, conditions.getString(i), size, factionId));

            }

        // Add industries and, if applicable, their specials
        JSONArray industries = marketOptions.optJSONArray(OPT_INDUSTRIES);
        if (industries != null) for (int i = 0; i < industries.length(); i++) {
            // Get the industry ID from a lone String or the first entry of a JSONArray
            JSONArray specials = industries.optJSONArray(i);
            String industryId;
            if (specials != null) industryId = specials.getString(0);
            else industryId = industries.optString(i, null);

            // Add the industry
            try {
                planetMarket.addIndustry(industryId);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_INDUSTRY, industryId, size, factionId));
            }

            // Add any special items, if applicable
            if (specials != null && specials.length() > 1) {
                Industry newIndustry = planetMarket.getIndustry(industryId);

                String aiCoreId = specials.optString(1, null);
                if (aiCoreId != null) newIndustry.setAICoreId(aiCoreId);

                String specialItem = specials.optString(2, null);
                if (specialItem != null) newIndustry.setSpecialItem(new SpecialItemData(specialItem, null));

                if (specials.length() > 3) newIndustry.setImproved(specials.optBoolean(3, false));
            }
        }
        else { // Just give market the bare minimum colony
            planetMarket.addIndustry(Industries.POPULATION);
            planetMarket.addIndustry(Industries.SPACEPORT);
        }

        // Add the appropriate submarkets
        planetMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        if (factionId.equals(Factions.PLAYER)) {
            planetMarket.setPlayerOwned(true);
            planetMarket.addSubmarket(Submarkets.LOCAL_RESOURCES);
            ((StoragePlugin) planetMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            marketsToOverrideAdmin.put(planetMarket, Factions.PLAYER);
        } else {
            planetMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
            if (planetMarket.hasIndustry(Industries.MILITARYBASE) || planetMarket.hasIndustry(Industries.HIGHCOMMAND))
                planetMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
            planetMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
        }

        // Adds an AI core admin to the market if enabled
        if (marketOptions.optBoolean(OPT_AI_CORE_ADMIN, false))
            marketsToOverrideAdmin.put(planetMarket, Commodities.ALPHA_CORE);

        //set market in global, factions, and assign market, also submarkets
        Global.getSector().getEconomy().addMarket(planetMarket, true);
        entity.setMarket(planetMarket);
        entity.setFaction(factionId);
    }

    /**
     * Adds a solar array near a planet, taking into account planetary conditions
     * <p>Look in com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator's
     * addSolarShadesAndMirrors() for vanilla implementation</p>
     *
     * @param planet    Planet to modify
     * @param factionId Faction owning the solar array
     */
    public void addSolarArray(PlanetAPI planet, String factionId) {
        int numOfShades = 0;
        int numOfMirrors = 0;
        String planetType = planet.getTypeId();
        StarSystemAPI system = planet.getStarSystem();
        String starType = system.getStar().getTypeId();
        if (planet.hasCondition(Conditions.HOT) || planetType.equals(Planets.DESERT) || planetType.equals(Planets.DESERT1) || planetType.equals(Planets.ARID) || starType.equals(StarTypes.BLUE_GIANT) || starType.equals(StarTypes.BLUE_SUPERGIANT))
            numOfShades = (StarSystemGenerator.random.nextBoolean() ? 3 : 1);

        if (planet.hasCondition(Conditions.POOR_LIGHT) || planetType.equals(Planets.PLANET_TERRAN_ECCENTRIC) || starType.equals(StarTypes.RED_DWARF) || starType.equals(StarTypes.BROWN_DWARF))
            numOfMirrors = (StarSystemGenerator.random.nextBoolean() ? 5 : 3);

        // Force a solar array if none of the above conditions are met
        if (numOfShades == 0 && numOfMirrors == 0) {
            numOfShades = 1;
            numOfMirrors = 3;
        }

        float radius = 270f + planet.getRadius();
        float angle = planet.getCircularOrbitAngle();
        float planetOrbitPeriod = planet.getCircularOrbitPeriod();

        // Create solar shades
        if (numOfShades >= 1) {
            SectorEntityToken shade2 = system.addCustomEntity(null, NAME_SHADE2, Entities.STELLAR_SHADE, factionId);
            shade2.setCircularOrbitPointingDown(planet, angle + 180, radius + 25, planetOrbitPeriod);
            if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE))
                MiscellaneousThemeGenerator.makeDiscoverable(shade2, 300f, 2000f);

            if (numOfShades == 3) {
                SectorEntityToken shade1 = system.addCustomEntity(null, NAME_SHADE1, Entities.STELLAR_SHADE, factionId);
                SectorEntityToken shade3 = system.addCustomEntity(null, NAME_SHADE3, Entities.STELLAR_SHADE, factionId);
                shade1.setCircularOrbitPointingDown(planet, angle + 154, radius - 10, planetOrbitPeriod);
                shade3.setCircularOrbitPointingDown(planet, angle + 206, radius - 10, planetOrbitPeriod);
                if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                    MiscellaneousThemeGenerator.makeDiscoverable(shade1, 300f, 2000f);
                    MiscellaneousThemeGenerator.makeDiscoverable(shade3, 300f, 2000f);
                }
            }
        }

        // Create solar mirrors
        if (numOfMirrors >= 3) {
            SectorEntityToken mirror2 = system.addCustomEntity(null, NAME_MIRROR2, Entities.STELLAR_MIRROR, factionId);
            SectorEntityToken mirror3 = system.addCustomEntity(null, NAME_MIRROR3, Entities.STELLAR_MIRROR, factionId);
            SectorEntityToken mirror4 = system.addCustomEntity(null, NAME_MIRROR4, Entities.STELLAR_MIRROR, factionId);
            mirror2.setCircularOrbitPointingDown(planet, angle - 30, radius, planetOrbitPeriod);
            mirror3.setCircularOrbitPointingDown(planet, angle, radius, planetOrbitPeriod);
            mirror4.setCircularOrbitPointingDown(planet, angle + 30, radius, planetOrbitPeriod);
            if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                MiscellaneousThemeGenerator.makeDiscoverable(mirror2, 300f, 2000f);
                MiscellaneousThemeGenerator.makeDiscoverable(mirror3, 300f, 2000f);
                MiscellaneousThemeGenerator.makeDiscoverable(mirror4, 300f, 2000f);
            }

            if (numOfMirrors == 5) {
                SectorEntityToken mirror1 = system.addCustomEntity(null, NAME_MIRROR1, Entities.STELLAR_MIRROR, factionId);
                SectorEntityToken mirror5 = system.addCustomEntity(null, NAME_MIRROR5, Entities.STELLAR_MIRROR, factionId);
                mirror1.setCircularOrbitPointingDown(planet, angle - 60, radius, planetOrbitPeriod);
                mirror5.setCircularOrbitPointingDown(planet, angle + 60, radius, planetOrbitPeriod);
                if (factionId == null || factionId.equals(DEFAULT_FACTION_TYPE)) {
                    MiscellaneousThemeGenerator.makeDiscoverable(mirror1, 300f, 2000f);
                    MiscellaneousThemeGenerator.makeDiscoverable(mirror5, 300f, 2000f);
                }
            }
        }
    }

    /**
     * Gets a unique proc-gen name; should only be called if it WILL be used, as the name cannot be picked again
     *
     * @param tag    Which name pool to draw from
     * @param parent What the name should depend on
     * @return A unique proc-gen name
     */
    public String getProcGenName(String tag, String parent) {
        String name = ProcgenUsedNames.pickName(tag, parent, null).nameWithRomanSuffixIfAny;
        ProcgenUsedNames.notifyUsed(name);
        return name;
    }

    /**
     * Sets a star system's type
     *
     * @param system The star system to modify
     */
    public void setSystemType(StarSystemAPI system) {
        int starCounter = 0;
        for (PlanetAPI body : system.getPlanets())
            if (body.isStar()) {
                starCounter++;
                if (starCounter == 2) { // Found at least 2 stars
                    system.setSecondary(body);

                    if (body.getId().contains(ID_STAR)) system.setType(StarSystemGenerator.StarSystemType.BINARY_FAR);
                    else system.setType(StarSystemGenerator.StarSystemType.BINARY_CLOSE);
                } else if (starCounter == 3) { // Found at least 3 stars
                    system.setTertiary(body);

                    if (body.getId().contains(ID_STAR)) { // Orbiting, or far star
                        if (system.getType() == StarSystemGenerator.StarSystemType.BINARY_FAR)
                            system.setType(StarSystemGenerator.StarSystemType.TRINARY_2FAR);
                        else // Current StarSystemType is BINARY_CLOSE
                            system.setType(StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR);
                    } else system.setType(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE);

                    break; // Stop loop; vanilla game only officially supports trinary systems at most
                }
            }
    }

    /**
     * Sets a system's light color based on a list of stars
     *
     * @param system      The system to modify
     * @param systemStars The stars the light color will be based on
     */
    public void setLightColor(StarSystemAPI system, List<PlanetAPI> systemStars) {
        Color result = Color.WHITE;
        for (int i = 0; i < systemStars.size(); i++)
            if (i != 0) result = Misc.interpolateColor(result, pickLightColorForStar(systemStars.get(i)), 0.5f);
            else result = pickLightColorForStar(systemStars.get(i)); // Set color to first star
        system.setLightColor(result); // light color in entire system, affects all entities
    }

    // Gets a star's light color
    private Color pickLightColorForStar(PlanetAPI star) {
        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), true);
        return Misc.interpolateColor(starData.getLightColorMin(), starData.getLightColorMax(), StarSystemGenerator.random.nextFloat());
    }

    /**
     * Adds an appropriate Remnant warning beacon to a system. Will do nothing if system has no THEME_REMNANT_... tags
     *
     * @param system The system to modify
     */
    public void addRemnantWarningBeacons(StarSystemAPI system) {
        if (system.hasTag(Tags.THEME_REMNANT_RESURGENT))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.RESURGENT);
        else if (system.hasTag(Tags.THEME_REMNANT_SUPPRESSED))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.SUPPRESSED);
        else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED))
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.DESTROYED);
    }

    /**
     * Generates a system's hyperspace jump points and clears nearby nebula
     *
     * @param system Star system to modify
     */
    public void generateHyperspace(StarSystemAPI system) {
        system.autogenerateHyperspaceJumpPoints(true, false);

        // Clear nebula in hyperspace
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float totalRadius = system.getMaxRadiusInHyperspace() + plugin.getTileSize() * 2f;
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, totalRadius, 0f, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, totalRadius, 0f, 360f, 0.25f);
    }

    /**
     * Set a star system's location to near the middle of a specified constellation
     * <p>Modified from the constellation proc-gen code originally made by Audax.</p>
     *
     * @param system           Star system to relocate
     * @param hyperspaceRadius Radius of star system in hyperspace
     * @param index            Index of constellation to set as location; if index <= 0, set system location to a random constellation.
     */
    public void setLocation(StarSystemAPI system, float hyperspaceRadius, int index) {
        // Get all proc-gen constellations in Sector hyperspace, sorted by proximty to the Core Worlds
        // Subsequent setLocation() calls should already have the full constellation list
        if (procGenConstellations == null) {
            // TreeSet orders the constellations by distance to Core Worlds, while also avoiding duplicates
            TreeSet<Constellation> sortedSet = new TreeSet<>(new Comparator<Constellation>() {
                public int compare(Constellation c1, Constellation c2) {
                    if (c1 == c2) return 0;
                    return Float.compare(Misc.getDistance(CORE_WORLD_CENTER, c1.getLocation()), Misc.getDistance(CORE_WORLD_CENTER, c2.getLocation()));
                }
            });
            for (StarSystemAPI sys : Global.getSector().getStarSystems())
                if (sys.isProcgen() && sys.isInConstellation()) sortedSet.add(sys.getConstellation());

            // Make new ArrayList to copy and store the ordered set of proc-gen constellations
            procGenConstellations = new ArrayList<>(sortedSet);
        }

        // If no constellations exist (for whatever reason), just set location to middle of Core Worlds
        // (you could consider them a special constellation?)
        if (procGenConstellations.isEmpty()) {
            system.getLocation().set(CORE_WORLD_CENTER);
            return;
        }

        // Select the constellation
        Constellation selectedConstellation;
        Random randomSeed = StarSystemGenerator.random;
        if (index <= 0) // Set location to a random constellation
            selectedConstellation = procGenConstellations.get(randomSeed.nextInt(procGenConstellations.size()));
        else // Set location to a specified constellation
            selectedConstellation = procGenConstellations.get(Math.min(index, procGenConstellations.size()) - 1);

        // Get centroid point of the selected constellation
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

        // Nudge the centroid point to a nearby random location
        centroidX += (randomSeed.nextBoolean() ? 1 : -1) * randomSeed.nextFloat() * 2000f;
        centroidY += (randomSeed.nextBoolean() ? 1 : -1) * randomSeed.nextFloat() * 2000f;

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

            // Found an empty location
            if (!intersects) newLoc = new Vector2f(thisX, thisY);
            else switch (move) { // Else pick the next location to check
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
            }
        }

        // Generate system as part of the selected constellation
        nearestSystems.add(system); // Selected constellation now contains the new system
        system.setConstellation(selectedConstellation);
        system.getLocation().set(newLoc);
        system.setAge(selectedConstellation.getAge());
    }
}