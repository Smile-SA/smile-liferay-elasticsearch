package fr.smile.liferay.elasticsearch.management;

/**
 * Created by cattez on 10/05/2017.
 */
public enum IndexAction {

    REINDEX,
    CHECK_STATUS,
    GET_MAPPINGS,
    GET_SETTINGS;

    /**
     * Check if a string represents a valid action.
     * @param value the supposed action in string format
     * @return <true> if value is a valid action, <false> otherwise
     */
    public static boolean contains(String value) {
        if (!(value == null || value.isEmpty())) {
            for (IndexAction action : values()) {
                if (value.equalsIgnoreCase(action.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the action relative to its string format.
     * @param value the supposed action in string format
     * @return an instance of {@link IndexAction} if value is a valid action, null otherwise
     */
    public static IndexAction get(String value) {
        if (!(value == null || value.isEmpty())) {
            for (IndexAction action : values()) {
                if (value.equalsIgnoreCase(action.name())) {
                    return action;
                }
            }
        }
        return null;
    }

}
