package com.lukicdarkoo.awscognitosample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public static final String AMAZON_COGNITO_IDENTITY_POOL_ID = "us-east-1_lasSptmFt";

    // `User Pools` is consider as an another Authentication providers for `Federated Identities`
    // therefor we need to create new User Pools and connect it with the Federated Identities
    public static final String CLIENT_ID = "1p040bq59amt2podihlpg831e";
    public static final String CLIENT_SECRET = "1f7v118jsrh156nek2ui685rt29gmijuto09j0q36qh8kjtsqsit";


    // Our imaginary user
    public static final String USERNAME = "awsuser";
    public static final String PASSWORD = "awspassword";
    public static final String EMAIL = "aws@email.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSTestAuthentication();
        //AWSTestSignup();
    }

    private void AWSTestAuthentication() {
        Log.e(TAG, "AWSTestAuthentication() - Start");

        CognitoUserPool userPool = new CognitoUserPool(this, AMAZON_COGNITO_IDENTITY_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        final CognitoUser cognitoUser = userPool.getUser(USERNAME);


        final GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();
                HashMap<String, String> hashMap = (HashMap<String, String>) cognitoUserAttributes.getAttributes();
                Log.d(TAG, hashMap.get("email"));
            }

            @Override
            public void onFailure(Exception exception) {

            }
        };

        // Callback handler for the sign-in process
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.d(TAG, "Authentication success");

                cognitoUser.getDetailsInBackground(detailsHandler);
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                // The API needs user sign-in credentials to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, PASSWORD, null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(final MultiFactorAuthenticationContinuation continuation) {
                continuation.continueTask();
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(TAG, "AWSTestAuthentication() - Failure: " + exception.getMessage());
            }
        };

        // Sign-in the user
        cognitoUser.getSessionInBackground(authenticationHandler);

        Log.e(TAG, "AWSTestAuthentication() - End");
    }


    // https://mobile.awsblog.com/post/TxNYVQQ3A2LT6Y/Using-Android-SDK-with-Amazon-Cognito-Your-User-Pools
    // http://docs.aws.amazon.com/cognito/latest/developerguide/using-amazon-cognito-user-identity-pools-android-sdk.html#using-amazon-cognito-user-identity-pools-android-sdk-user-pool-get-tokens
    private void AWSTestSignup() {
        Log.e(TAG, "AWSTestSignup() - Start");

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        // Create attributes for user
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("email", EMAIL);

        SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                Log.d(TAG, "AWSTestSignup() - Success");
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d(TAG, "AWSTestSignup() - Failure: " + exception.getMessage().toString());
            }
        };


        // http://docs.aws.amazon.com/AWSAndroidSDK/latest/javadoc/com/amazonaws/mobileconnectors/cognitoidentityprovider/CognitoUserPool.html
        CognitoUserPool userPool = new CognitoUserPool(this, AMAZON_COGNITO_IDENTITY_POOL_ID, CLIENT_ID, CLIENT_SECRET, clientConfiguration);
        userPool.signUpInBackground(USERNAME, PASSWORD, userAttributes, null, signupCallback);

        Log.e(TAG, "AWSTestSignup() - Finished");
    }
}
