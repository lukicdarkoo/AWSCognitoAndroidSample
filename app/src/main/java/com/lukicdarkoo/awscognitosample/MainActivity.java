package com.lukicdarkoo.awscognitosample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public static final String AMAZON_COGNITO_IDENTITY_POOL_ID = "us-east-1:55399e2c-dff5-45b7-9552-27d1631bfd1f";
    public static final String AMAZON_COGNITO_USER_POOL_ID = "us-east-1_lasSptmFt";
    public static final Regions REGION = Regions.US_EAST_1;

    // `User Pools` is consider as an another Authentication providers for `Federated Identities`
    // therefor we need to create new User Pools and connect it with the Federated Identities
    public static final String CLIENT_ID = "1p040bq59amt2podihlpg831e";
    public static final String CLIENT_SECRET = "1f7v118jsrh156nek2ui685rt29gmijuto09j0q36qh8kjtsqsit";


    // Our imaginary user
    public static final String USERNAME = "awsuser2";
    public static final String PASSWORD = "awspassword2";
    public static final String EMAIL = "aws2@email.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSTestAuthentication();
        //AWSTestSignup();
    }


    // http://docs.aws.amazon.com/cognito/latest/developerguide/synchronizing-data.html
    private void AWSTestSync(String token) {
        Log.d(TAG, "AWSTestSync() - Start");

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
            this,
            AMAZON_COGNITO_IDENTITY_POOL_ID,
            REGION);

        // We want to associate Cognito Sync data with user in User Pool
        // http://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-integrating-user-pools-with-identity-pools.html
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("cognito-idp." + REGION.toString() + ".amazonaws.com/" + AMAZON_COGNITO_USER_POOL_ID, token);
        credentialsProvider.setLogins(logins);


        // Let's use Cognito Sync
        CognitoSyncManager client = new CognitoSyncManager(
                this,
                REGION,
                credentialsProvider);

        Dataset dataset = client.openOrCreateDataset("test");
        dataset.put("answers", "123456789");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                Log.d(TAG, "Data successfully synced");
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                Log.e(TAG, "There is dataset conflict");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                return false;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                return false;
            }

            @Override
            public void onFailure(DataStorageException dse) {
                Log.e(TAG, "Data sync failure - " + dse.getMessage());
            }
        });

        Log.d(TAG, "AWSTestSync() - End");
    }

    private void AWSTestAuthentication() {
        Log.e(TAG, "AWSTestAuthentication() - Start");

        CognitoUserPool userPool = new CognitoUserPool(this, AMAZON_COGNITO_USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        final CognitoUser cognitoUser = userPool.getUser(USERNAME);


        // Callback handler for the sign-in process
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.d(TAG, "Authentication success");

                // Test Cognito Sync
                AWSTestSync(userSession.getAccessToken().getJWTToken());
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
