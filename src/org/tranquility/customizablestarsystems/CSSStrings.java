package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;

/**
 * A class containing the strings.json entries for this mod
 */
public final class CSSStrings {
    private static final String STRINGS_CATEGORY = "customizablestarsystems";

    public static final String MOD_ID_CUSTOMIZABLE_STAR_SYSTEMS = Global.getSettings().getString(STRINGS_CATEGORY, "mod_id_customizablestarsystems");

    // Settings in settings.json
    public static final String SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS = Global.getSettings().getString(STRINGS_CATEGORY, "settings_enableCustomStarSystems");
    public static final String SETTINGS_HYPERSPACE_CENTER = Global.getSettings().getString(STRINGS_CATEGORY, "settings_hyperspaceCenter");
    public static final String SETTINGS_SYSTEM_SPACING = Global.getSettings().getString(STRINGS_CATEGORY, "settings_systemSpacing");

    // Main system options in customStarSystems.json
    public static final String OPT_IS_ENABLED = Global.getSettings().getString(STRINGS_CATEGORY, "opt_isEnabled");
    public static final String OPT_NUMBER_OF_SYSTEMS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_numberOfSystems");
    public static final String OPT_HAS_SYSTEMWIDE_NEBULA = Global.getSettings().getString(STRINGS_CATEGORY, "opt_hasSystemwideNebula");
    public static final String OPT_ADD_CORONAL_HYPERSHUNT = Global.getSettings().getString(STRINGS_CATEGORY, "opt_addCoronalHypershunt");
    public static final String OPT_ADD_DOMAIN_CRYOSLEEPER = Global.getSettings().getString(STRINGS_CATEGORY, "opt_addDomainCryosleeper");
    public static final String OPT_SET_LOCATION = Global.getSettings().getString(STRINGS_CATEGORY, "opt_setLocation");
    public static final String OPT_SYSTEM_AGE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_systemAge");
    public static final String OPT_SYSTEM_BACKGROUND = Global.getSettings().getString(STRINGS_CATEGORY, "opt_systemBackground");
    public static final String OPT_SYSTEM_MUSIC = Global.getSettings().getString(STRINGS_CATEGORY, "opt_systemMusic");
    public static final String OPT_SYSTEM_LIGHT_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_systemLightColor");
    public static final String OPT_TELEPORT_UPON_GENERATION = Global.getSettings().getString(STRINGS_CATEGORY, "opt_teleportUponGeneration");
    public static final String OPT_SYSTEM_TAGS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_system_tags");
    public static final String OPT_ENTITIES = Global.getSettings().getString(STRINGS_CATEGORY, "opt_entities");

    // Sub-options
    public static final String OPT_ENTITY = Global.getSettings().getString(STRINGS_CATEGORY, "opt_entity");
    public static final String OPT_NUM_OF_CENTER_STARS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_numOfCenterStars");
    public static final String OPT_ORBIT_RADIUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_orbitRadius");
    public static final String OPT_NAME = Global.getSettings().getString(STRINGS_CATEGORY, "opt_name");
    public static final String OPT_ORBIT_ANGLE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_orbitAngle");
    public static final String OPT_ORBIT_DAYS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_orbitDays");
    public static final String OPT_ORBIT_CLOCKWISE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_orbitClockwise");
    public static final String OPT_TYPE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_type");
    public static final String OPT_RADIUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_radius");
    public static final String OPT_CORONA_RADIUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_coronaRadius");
    public static final String OPT_FLARE_CHANCE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_flareChance");
    public static final String OPT_SPEC_CHANGES = Global.getSettings().getString(STRINGS_CATEGORY, "opt_specChanges");
    public static final String OPT_CUSTOM_DESCRIPTION_ID = Global.getSettings().getString(STRINGS_CATEGORY, "opt_customDescriptionId");
    public static final String OPT_FOCUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_focus");
    public static final String OPT_CONDITIONS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_conditions");
    public static final String OPT_MARKET_SIZE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_marketSize");
    public static final String OPT_FACTION_ID = Global.getSettings().getString(STRINGS_CATEGORY, "opt_factionId");
    public static final String OPT_FREE_PORT = Global.getSettings().getString(STRINGS_CATEGORY, "opt_freePort");
    public static final String OPT_AI_CORE_ADMIN = Global.getSettings().getString(STRINGS_CATEGORY, "opt_aiCoreAdmin");
    public static final String OPT_INDUSTRIES = Global.getSettings().getString(STRINGS_CATEGORY, "opt_industries");
    public static final String OPT_MEMORY_KEYS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_memoryKeys");
    public static final String OPT_MARKET_MEMORY_KEYS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_marketMemoryKeys");
    public static final String OPT_TAGS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_tags");

    // Sub-options for "specChanges"
    public static final String OPT_ATMOSPHERE_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_atmosphereColor");
    public static final String OPT_ATMOSPHERE_THICKNESS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_atmosphereThickness");
    public static final String OPT_ATMOSPHERE_THICKNESS_MIN = Global.getSettings().getString(STRINGS_CATEGORY, "opt_atmosphereThicknessMin");
    public static final String OPT_CLOUD_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_cloudColor");
    public static final String OPT_CLOUD_ROTATION = Global.getSettings().getString(STRINGS_CATEGORY, "opt_cloudRotation");
    public static final String OPT_CLOUD_TEXTURE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_cloudTexture");
    public static final String OPT_GLOW_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_glowColor");
    public static final String OPT_GLOW_TEXTURE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_glowTexture");
    public static final String OPT_ICON_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_iconColor");
    public static final String OPT_PITCH = Global.getSettings().getString(STRINGS_CATEGORY, "opt_pitch");
    public static final String OPT_PLANET_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_planetColor");
    public static final String OPT_ROTATION = Global.getSettings().getString(STRINGS_CATEGORY, "opt_rotation");
    public static final String OPT_TEXTURE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_texture");
    public static final String OPT_TILT = Global.getSettings().getString(STRINGS_CATEGORY, "opt_tilt");
    public static final String OPT_USE_REVERSE_LIGHT_FOR_GLOW = Global.getSettings().getString(STRINGS_CATEGORY, "opt_useReverseLightForGlow");
    public static final String OPT_TYPE_OVERRIDE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_typeOverride");

    // Sub-options for certain entities
    public static final String OPT_IS_DAMAGED = Global.getSettings().getString(STRINGS_CATEGORY, "opt_isDamaged");
    public static final String OPT_SIZE = Global.getSettings().getString(STRINGS_CATEGORY, "opt_size");
    public static final String OPT_NUM_ASTEROIDS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_numAsteroids");
    public static final String OPT_VARIANT_ID = Global.getSettings().getString(STRINGS_CATEGORY, "opt_variantId");
    public static final String OPT_INNER_BAND_INDEX = Global.getSettings().getString(STRINGS_CATEGORY, "opt_innerBandIndex");
    public static final String OPT_OUTER_BAND_INDEX = Global.getSettings().getString(STRINGS_CATEGORY, "opt_outerBandIndex");
    public static final String OPT_BAND_INDEX = Global.getSettings().getString(STRINGS_CATEGORY, "opt_bandIndex");
    public static final String OPT_MIDDLE_RADIUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_middleRadius");
    public static final String OPT_OUTER_RADIUS = Global.getSettings().getString(STRINGS_CATEGORY, "opt_outerRadius");
    public static final String OPT_BASE_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_baseColor");
    public static final String OPT_AURORA_FREQUENCY = Global.getSettings().getString(STRINGS_CATEGORY, "opt_auroraFrequency");
    public static final String OPT_AURORA_COLOR = Global.getSettings().getString(STRINGS_CATEGORY, "opt_auroraColor");

    // Default types/paths
    public static final String DEFAULT_STAR_TYPE = Global.getSettings().getString(STRINGS_CATEGORY, "default_star_type");
    public static final String DEFAULT_PLANET_TYPE = Global.getSettings().getString(STRINGS_CATEGORY, "default_planet_type");
    public static final String DEFAULT_FACTION_TYPE = Global.getSettings().getString(STRINGS_CATEGORY, "default_faction_type");
    public static final String DEFAULT_ACCRETION_DISK_NAME = Global.getSettings().getString(STRINGS_CATEGORY, "default_accretion_disk_name");
    public static final String DEFAULT_CRYOSLEEPER_NAME = Global.getSettings().getString(STRINGS_CATEGORY, "default_cryosleeper_name");
    public static final String DEFAULT_STATION_TYPE = Global.getSettings().getString(STRINGS_CATEGORY, "default_station_type");
    public static final String PATH_GRAPHICS_PLANET = Global.getSettings().getString(STRINGS_CATEGORY, "path_graphics_planet");
    public static final String PATH_GRAPHICS_BACKGROUND = Global.getSettings().getString(STRINGS_CATEGORY, "path_graphics_background");
    public static final String PATH_MERGED_JSON_CUSTOM_STAR_SYSTEMS = Global.getSettings().getString(STRINGS_CATEGORY, "path_merged_json_customStarSystems");

    // Names used in Solar Array generation
    public static final String NAME_MIRROR1 = Global.getSettings().getString(STRINGS_CATEGORY, "name_mirror1");
    public static final String NAME_MIRROR2 = Global.getSettings().getString(STRINGS_CATEGORY, "name_mirror2");
    public static final String NAME_MIRROR3 = Global.getSettings().getString(STRINGS_CATEGORY, "name_mirror3");
    public static final String NAME_MIRROR4 = Global.getSettings().getString(STRINGS_CATEGORY, "name_mirror4");
    public static final String NAME_MIRROR5 = Global.getSettings().getString(STRINGS_CATEGORY, "name_mirror5");
    public static final String NAME_SHADE1 = Global.getSettings().getString(STRINGS_CATEGORY, "name_shade1");
    public static final String NAME_SHADE2 = Global.getSettings().getString(STRINGS_CATEGORY, "name_shade2");
    public static final String NAME_SHADE3 = Global.getSettings().getString(STRINGS_CATEGORY, "name_shade3");
    public static final String NAME_DAMAGED_STATION = Global.getSettings().getString(STRINGS_CATEGORY, "name_damaged_station");

    // Other, used in code
    public static final String SOURCE_GEN = Global.getSettings().getString(STRINGS_CATEGORY, "source_gen");
    public static final String CATEGORY_MISC = Global.getSettings().getString(STRINGS_CATEGORY, "category_misc");

    // Error messages
    public static final String ERROR_BAD_CENTER_STAR = Global.getSettings().getString(STRINGS_CATEGORY, "error_badCenterStar");
    public static final String ERROR_STAR_TYPE_NOT_FOUND = Global.getSettings().getString(STRINGS_CATEGORY, "error_starTypeNotFound");
    public static final String ERROR_PLANET_TYPE_NOT_FOUND = Global.getSettings().getString(STRINGS_CATEGORY, "error_planetTypeNotFound");
    public static final String ERROR_INVALID_FOCUS = Global.getSettings().getString(STRINGS_CATEGORY, "error_invalidFocus");
    public static final String ERROR_INVALID_CONDITION_UNINHABITED = Global.getSettings().getString(STRINGS_CATEGORY, "error_invalidConditionUninhabited");
    public static final String ERROR_INVALID_CONDITION_INHABITED = Global.getSettings().getString(STRINGS_CATEGORY, "error_invalidConditionInhabited");
    public static final String ERROR_INVALID_INDUSTRY = Global.getSettings().getString(STRINGS_CATEGORY, "error_invalidIndustry");
    public static final String ERROR_INVALID_ENTITY_ID = Global.getSettings().getString(STRINGS_CATEGORY, "error_invalidEntityID");

    // Console Commands messages
    public static final String COMMANDS_GENERATED_SYSTEM = Global.getSettings().getString(STRINGS_CATEGORY, "commands_generatedSystem");
    public static final String COMMANDS_DISABLED_SYSTEM = Global.getSettings().getString(STRINGS_CATEGORY, "commands_disabledSystem");
    public static final String COMMANDS_ERROR_BAD_JSON = Global.getSettings().getString(STRINGS_CATEGORY, "commands_error_badJSON");
    public static final String COMMANDS_ERROR_BAD_SYSTEM = Global.getSettings().getString(STRINGS_CATEGORY, "commands_error_badSystem");
    public static final String COMMANDS_ERROR_NO_SYSTEM_ID = Global.getSettings().getString(STRINGS_CATEGORY, "commands_error_noSystemID");

    // LunaSnippets
    public static final String SNIPPETS_SPAWN_SYSTEM_NAME = Global.getSettings().getString(STRINGS_CATEGORY, "snippets_spawnSystemName");
    public static final String SNIPPETS_SPAWN_SYSTEM_DESC = Global.getSettings().getString(STRINGS_CATEGORY, "snippets_spawnSystemDesc");
    public static final String SNIPPETS_SPAWN_SYSTEM_NO_SELECTED = Global.getSettings().getString(STRINGS_CATEGORY, "snippets_spawnSystemNoSelected");
}
