package org.jorge_rico_vivas.java_extender.string;


class StringFormatTest {

    public static void main(String[] args) {
        String unformatted = "Current situation of {meta.user -> a: My user jorge, : Another user} is: {current_state}\\n\\nLast action was: {last_action}\\nActions to take: {number_of_actions} {actions_to_take -> actions_to_take>1|0: actions, default: action}\\n\\nRecommended action is: {recommended_action}[current_state, last_action, number_of_actions, actions_to_take, recommended_action]";
        StringFormatMetadataMap metadata = new StringFormatMetadataMap();
        metadata.getMetadata().put("user", "Jorge");
        String res = StringFormat.apply(metadata, unformatted, "My state", "My action", 1, 1, "My recommended action");
        System.out.println(res);
    }

}