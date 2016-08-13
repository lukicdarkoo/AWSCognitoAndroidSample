package com.lukicdarkoo.awscognitosample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public static final String AMAZON_COGNITO_IDENTITY_POOL_ID =
            "us-east-1:55399e2c-dff5-45b7-9552-27d1631bfd1f";

    // `User Pools` is consider as an another Authentication providers for `Federated Identities`
    // therefor we need to create new User Pools and connect it with the Federated Identities
    public static final String CLIENT_ID = "432e9lom3sfgn1va4d8nnk6koi";
    public static final String CLIENT_SECRET = "10i39rpaet784jdf42d6i29dqe7f3j7gkqhrhecnlivb8s8d5n15";


    // Our imaginary user
    public static final String USERNAME = "awsuser";
    public static final String PASSWORD = "awspassword";
    public static final String EMAIL = "aws@email.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSTestAuthentication();
    }

    private void AWSTestAuthentication() {
        Log.e(TAG, "AWSTestAuthentication() - Start");

        CognitoUserPool userPool = new CognitoUserPool(this, AMAZON_COGNITO_IDENTITY_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        CognitoUser cognitoUser = userPool.getUser("lukicdarkoo");

        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession cognitoUserSession) {
                Log.e(TAG, "AWSTestAuthentication() - Success");
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                // The API needs user sign-in credentials to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(USERNAME, PASSWORD, null);

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
        userPool.signUpInBackground(USERNAME, EMAIL, userAttributes, null, signupCallback);

        Log.e(TAG, "AWSTestSignup() - Finished");
    }
}
