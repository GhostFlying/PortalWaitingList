package com.ghostflying.portalwaitinglist.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ghost on 2014/12/6.
 * <br>
 * The class is a util for regex used in the app.
 */
public class RegexUtil {
    private static RegexUtil instance;
    private static final String REGEX_PORTAL_SUBMISSION = "(?<=Ingress Portal Submitted:).+";
    private static final String REGEX_PORTAL_EDIT = "(?<=Ingress Portal Edits Submitted:).+";
    private static final String REGEX_INVALID_REPORT = "(?<=Invalid Ingress Portal Report:).+";
    private static final String REGEX_PORTAL_SUBMISSION_PASSED = "(?<=Ingress Portal Live:).+";
    private static final String REGEX_PORTAL_SUBMISSION_REJECTED = "(?<=Ingress Portal Rejected:).+";
    private static final String REGEX_PORTAL_SUBMISSION_DUPLICATE = "(?<=Ingress Portal Duplicate:).+";
    private static final String REGEX_PORTAL_EDIT_PASSED = "(?<=Ingress Portal Data Edit Accepted:).+";
    private static final String REGEX_PORTAL_EDIT_REJECTED = "(?<=Ingress Portal Data Edit Reviewed:).+";

    private static final String REGEX_NEW_PORTAL_SUBMISSION = "(?<=Portal submission confirmation: ).+";
    private static final String REGEX_NEW_PORTAL_SUBMISSION_REVIEWED = "(?<=Portal review complete:).+";
    private static final String REGEX_NEW_PORTAL_SUBMISSION_ACCEPTED = "we've accepted your submission";
    private static final String REGEX_NEW_PORTAL_SUBMISSION_REJECTED = "we have decided not to accept this candidate.";
    private static final String REGEX_NEW_PORTAL_EDIT = "(?<=Portal edit submission confirmation:).+";

    private static final String REGEX_EACH_JSON_IN_BATCH = "\\{.+\\}";
    private static final String REGEX_FIND_BOUNDARY = "(?<=boundary=).+";
    private static final String REGEX_IMG_URL = "(?<=<img src=\").+(?=\" alt)";
    private static final String REGEX_ADDRESS = "(?<=z=18\">).+(?=</a>)";
    private static final String REGEX_ADDRESS_URL = "https://www.ingress.com/intel.+z=18";
    private static final String[] REGEXS = {
            REGEX_PORTAL_SUBMISSION,
            REGEX_PORTAL_EDIT,
            REGEX_INVALID_REPORT,
            REGEX_PORTAL_SUBMISSION_PASSED,
            REGEX_PORTAL_SUBMISSION_REJECTED,
            REGEX_PORTAL_SUBMISSION_DUPLICATE,
            REGEX_PORTAL_EDIT_PASSED,
            REGEX_PORTAL_EDIT_REJECTED,
            REGEX_EACH_JSON_IN_BATCH,
            REGEX_FIND_BOUNDARY,
            REGEX_IMG_URL,
            REGEX_ADDRESS,
            REGEX_ADDRESS_URL,
            REGEX_NEW_PORTAL_SUBMISSION,
            REGEX_NEW_PORTAL_SUBMISSION_REVIEWED,
            REGEX_NEW_PORTAL_SUBMISSION_ACCEPTED,
            REGEX_NEW_PORTAL_SUBMISSION_REJECTED,
            REGEX_NEW_PORTAL_EDIT
    };
    static final int PORTAL_SUBMISSION = 0;
    static final int PORTAL_EDIT = 1;
    static final int INVALID_REPORT = 2;
    static final int PORTAL_SUBMISSION_PASSED = 3;
    static final int PORTAL_SUBMISSION_REJECTED = 4;
    static final int PORTAL_SUBMISSION_DUPLICATE = 5;
    static final int PORTAL_EDIT_PASSED = 6;
    static final int PORTAL_EDIT_REJECTED = 7;
    static final int EACH_JSON_IN_BATCH = 8;
    static final int FIND_BOUNDARY = 9;
    static final int IMG_URL = 10;
    static final int ADDRESS = 11;
    static final int ADDRESS_URL = 12;
    static final int NEW_PORTAL_SUBMISSION = 13;
    static final int NEW_PORTAL_SUBMISSION_REVIEWED = 14;
    static final int NEW_PORTAL_SUBMISSION_ACCEPTED = 15;
    static final int NEW_PORTAL_SUBMISSION_REJECTED = 16;
    static final int NEW_PORTAL_EDIT = 17;

    private RegexPair[] regexPairs;
    private Matcher matcher;

    /**
     * Private constructor
     */
    private RegexUtil(){
        regexPairs = new RegexPair[REGEXS.length];
    }

    /**
     * Get the instance of RegexUtil
     * @return  the only instance of RegexUtil
     */
    public static RegexUtil getInstance(){
        if (instance == null)
            instance = new RegexUtil();
        return instance;
    }

    /**
     * Check if the str contains the string defined by reg.
     * @param reg   the reg defined.
     * @param str   the str to search.
     * @return  return true if found, otherwise false.
     */
    public boolean isFound(int reg, String str){
        if (regexPairs[reg] == null){
            regexPairs[reg] = new RegexPair(REGEXS[reg]);
        }
        matcher = regexPairs[reg].getPattern().matcher(str);
        return matcher.find();
    }

    /**
     * Get the last match str, must be called after {@link com.ghostflying.portalwaitinglist.util.RegexUtil#isFound(int, String)}
     * @return  the matched str.
     */
    public String getMatchedStr(){
        return matcher.group();
    }

    private class RegexPair{
        String regex;
        Pattern pattern;

        public RegexPair(String regex){
            this.regex = regex;
        }

        public Pattern getPattern(){
            if (pattern == null)
                compilePattern();
            return pattern;
        }

        private void compilePattern(){
            if (regex.equals(RegexUtil.REGEX_EACH_JSON_IN_BATCH)){
                pattern = Pattern.compile(regex, Pattern.DOTALL);
            }
            else {
                pattern = Pattern.compile(regex);
            }
        }
    }
}
