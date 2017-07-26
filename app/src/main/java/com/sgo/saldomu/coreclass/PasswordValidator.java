package com.sgo.saldomu.coreclass;

import com.sgo.saldomu.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuddistirakiki on 1/26/16.
 */
public class PasswordValidator {

//    private Pattern pattern;
    private Pattern pattern1;
    private Pattern pattern2;
    private Pattern pattern3;
    private Pattern pattern4;

    private Matcher matcher;

//    private static final String PASSWORD_PATTERN =
//            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,15})";

    private static final String PASSWORD_PATTERN1 =
            "(?=.*\\d)";
    private static final String PASSWORD_PATTERN2 =
            "(?=.*[a-z])";
    private static final String PASSWORD_PATTERN3 =
            "(?=.*[A-Z])";

    /*

    (			# Start of group
        (?=.*\d)		#   must contains one digit from 0-9
        (?=.*[a-z])		#   must contains one lowercase characters
        (?=.*[A-Z])		#   must contains one uppercase characters
        (?=.*[@#$%])    #   must contains one special symbols in the list "@#$%"
                  .		#   match anything with previous condition checking
                {6,20}	#   length at least 6 characters and maximum of 20
    )			        # End of group
     */

    public PasswordValidator(){
//        pattern = Pattern.compile(PASSWORD_PATTERN);
        pattern1 = Pattern.compile(PASSWORD_PATTERN1);
        pattern2 = Pattern.compile(PASSWORD_PATTERN2);
        pattern3 = Pattern.compile(PASSWORD_PATTERN3);
    }

    /**
     * Validate password with regular expression
     * @param password password for validation
     * @return true valid password, false invalid password
     */
    public boolean validate(String password){

        matcher = pattern1.matcher(password);
        return matcher.matches();

    }

    public int validates(String password){

        matcher = pattern1.matcher(password);
        if(!matcher.matches()){
            return R.string.password_valid_number;
        }
        matcher = pattern2.matcher(password);
        if(!matcher.matches()){
            return R.string.password_valid_lowcase;
        }

        matcher = pattern3.matcher(password);
        if(!matcher.matches()){
            return R.string.password_valid_lowcase;
        }

        return 0;
    }
}
