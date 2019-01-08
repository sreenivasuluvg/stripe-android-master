package com.stripe.example.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentIntentParams;
import com.stripe.android.model.SourceParams;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardNumberEditText;
import com.stripe.android.view.ExpiryDateEditText;
import com.stripe.android.view.StripeEditText;
import com.stripe.example.R;
import com.stripe.example.controller.ErrorDialogHandler;
import com.stripe.example.controller.ProgressDialogController;
import com.stripe.example.module.RetrofitFactory;
import com.stripe.example.service.StripeService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PaymentIntentActivity extends AppCompatActivity {
    private static final String TAG = PaymentIntentActivity.class.getName();

    private static final String RETURN_URL = "https://api.stripe.com";
    private ProgressDialogController mProgressDialogController;
    private ErrorDialogHandler mErrorDialogHandler;
    private CompositeSubscription mCompositeSubscription;
    private Stripe mStripe;
    private String number,test,test1,test3;
    private StripeService mStripeService;
    private String mClientSecret,Token;
    private Button mConfirmPaymentIntent;
    private Button mRetrievePaymentIntent;
    private CardInputWidget mCardInputWidget;
    private TextView mPaymentIntentValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_intent_demo);
        Button createPaymentIntent = findViewById(R.id.btn_create_payment_intent);
        mRetrievePaymentIntent = findViewById(R.id.btn_retrieve_payment_intent);
        mConfirmPaymentIntent = findViewById(R.id.btn_confirm_payment_intent);
        mPaymentIntentValue = findViewById(R.id.payment_intent_value);
        mCardInputWidget = findViewById(R.id.card_input_widget);
//mCardInputWidget.setCardInputListener();
  /*      mCardInputWidget = activityController.get().getCardInputWidget();
        mCardInputWidget.setDimensionOverrideSettings(dimensionOverrides);
        mOnGlobalFocusChangeListener = new TestFocusChangeListener();
        mCardInputWidget.getViewTreeObserver()
                .addOnGlobalFocusChangeListener(mOnGlobalFocusChangeListener);

        mCardNumberEditText = activityController.get().getCardNumberEditText();
        mCardNumberEditText.setText("");*/

        ExpiryDateEditText mExpiryEditText = mCardInputWidget.findViewById(R.id.et_expiry_date);
        StripeEditText mCvcEditText = mCardInputWidget.findViewById(R.id.et_cvc_number);
//                 test=   mExpiryEditText.getText().toString();
//                String test1=   mCvcEditText.getText().toString();

        mProgressDialogController =
                new ProgressDialogController(getSupportFragmentManager());

        mErrorDialogHandler = new ErrorDialogHandler(getSupportFragmentManager());
        mCompositeSubscription = new CompositeSubscription();
        mStripe = new Stripe(this);
        Retrofit retrofit = RetrofitFactory.getInstance();
        mStripeService = retrofit.create(StripeService.class);

        createPaymentIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPaymentIntent();
                CardNumberEditText  et_card_number=mCardInputWidget.findViewById(R.id.et_card_number);
                ExpiryDateEditText mExpiryEditText = mCardInputWidget.findViewById(R.id.et_expiry_date);
                StripeEditText mCvcEditText = mCardInputWidget.findViewById(R.id.et_cvc_number);
                 test=   et_card_number.getText().toString();
                test1=   mCvcEditText.getText().toString();
                test3=mExpiryEditText.getText().toString().substring(3);
                number=mExpiryEditText.getText().toString().substring(0,2);

            }
        });

        mRetrievePaymentIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrievePaymentIntent(mClientSecret);
            }
        });
         Token="Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImVjNjMzZjRhNWY5YzgyNzZmODM2ZDQxZjQxZDFjYTYwYjU1YjE4MzY5ODE1NWI4NTYwYmQ2NjRjZGY3ZDUzYzFhZTM1ZTQyNTk0MDcwNjA2In0.eyJhdWQiOiIyIiwianRpIjoiZWM2MzNmNGE1ZjljODI3NmY4MzZkNDFmNDFkMWNhNjBiNTViMTgzNjk4MTU1Yjg1NjBiZDY2NGNkZjdkNTNjMWFlMzVlNDI1OTQwNzA2MDYiLCJpYXQiOjE1MzU5ODEyODIsIm5iZiI6MTUzNTk4MTI4MiwiZXhwIjoxNTY3NTE3MjgyLCJzdWIiOiI1NyIsInNjb3BlcyI6W119.JgxqE3_Wa8fca-oM5tMZCWjusrDPzYHpeNgAEhC2FTvKXK9YSEwKjWzjrXcfQ8HTD00FUZLe6uHW8uyw3Ldy1uGq4ePwOBm6hEpzWtb0uKOFwrqMEkJHhagZcFiKeMhagRSor_eQtH2hpMlMxKAlDtoYWSYBUjqZUERxnbumsE2gCd7CSDYsluY7k_oW8y_9kVrFuoz0Zu-Harqpcsf3CDBTJrc6KTpE5NXtEv-KIUR7jZSIQGu6CTh5NQ-VeXDW74_8P9NwzBZfqMmPxeLdFARnKs6AJjCd12JpVW4o5GUq-UmAV6xEweNrt8ks7w0ovDTHXIZPgBxlpbvJotf_-d02BP1BIspt8cplXSg4oRuouNBIqKSXgA9mBx1bWs4GYsts2G347KtnG5xMK-B95-PefBXBp1uu-O-TTO6RfsvZzq0Yhu-N7fdph1MWJnXaUAHFLDcSv8zeFtFbakX6TxPgc6e1z-__B6LxUXvybtF_4BV5-eqMRiU5Q7qeFOsKdClFpBN-N9FTv1Inh1BVl6YGLkxiV--RmdPGtwt_M62WYMzLqFYAXHMpwGCr1PLb4dQHxCUTII_lbwVRyMAbe4y9LKlr29Zz7N1snb00hBkkAth-lBznAX0Ig3lmCsCKdmSiQZFexgu0RWRkYfI4bPEs_6nkZALfq5zHDT_wxMw";
        mConfirmPaymentIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card card = mCardInputWidget.getCard();
                if (card != null) {
                    confirmPaymentIntent(mClientSecret, card);
                }
            }
        });

    }

    private Map<String, Object> createPaymentIntentParams()
    {
        Map<String, Object> params = new HashMap<>();
        params.put("card_no", test);
        params.put("amount", 10);
        params.put("cvvNumber",test1);
        params.put("ccExpiryMonth",number);
        params.put("ccExpiryYear",test3);
        return params;                              //
    }
//    "card_no" :"4242424242424242",
//  "cvvNumber": 322,
//  "ccExpiryMonth": 10,
//  "ccExpiryYear":2022,
//  "amount":300

    void createPaymentIntent() {
        Subscription subscription = mStripeService.createPaymentIntent(Token,createPaymentIntentParams())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.setMessageResource(R.string.creating_payment_intent);
                        mProgressDialogController.startProgress();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.finishProgress();
                    }
                })
                .subscribe(
                        // Because we've made the mapping above, we're now subscribing
                        // to the result of creating a 3DS Source
                        new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody responseBody)
                            {
                                try {
                                    JSONObject jsonObject = new JSONObject(responseBody.string());
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    mClientSecret = data.optString("id");
                                    mConfirmPaymentIntent.setEnabled(mClientSecret != null);
                                    mRetrievePaymentIntent.setEnabled(mClientSecret != null);

                                } catch (IOException | JSONException exception) {
                                    Log.e(TAG, exception.toString());
                                }
                            }
                        },
                        new Action1<Throwable>()
                        {
                            @Override
                            public void call(Throwable throwable) {
                                mErrorDialogHandler.showError(throwable.getLocalizedMessage());
                            }
                        }
                );
        mCompositeSubscription.add(subscription);
    }

    void retrievePaymentIntent(final String clientSecret) {
        final Observable<PaymentIntent> paymentIntentObservable =
                Observable.fromCallable(
                        new Callable<PaymentIntent>() {
                            @Override
                            public PaymentIntent call() throws Exception {
                                PaymentIntentParams paymentIntentParams =
                                        PaymentIntentParams.createRetrievePaymentIntentParams(clientSecret);
                                return mStripe.retrievePaymentIntentSynchronous(
                                        paymentIntentParams,
                                        PaymentConfiguration.getInstance().getPublishableKey());
                            }
                        });
        Subscription subscription = paymentIntentObservable
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.setMessageResource(R.string.retrieving_payment_intent);
                        mProgressDialogController.startProgress();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.finishProgress();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        new Action1<PaymentIntent>() {
                            @Override
                            public void call(PaymentIntent paymentIntent) {
                                if (paymentIntent != null) {
                                    mPaymentIntentValue.setText(paymentIntent.toJson().toString());
                                } else {
                                    mPaymentIntentValue.setText(R.string.error_while_retrieving_payment_intent);
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, throwable.toString());
                            }
                        }
                );
        mCompositeSubscription.add(subscription);
    }

    void confirmPaymentIntent(final String clientSecret, Card card) {
        final SourceParams sourceParams = SourceParams.createCardParams(card);
        final Observable<PaymentIntent> paymentIntentObservable =
                Observable.fromCallable(
                        new Callable<PaymentIntent>() {
                            @Override
                            public PaymentIntent call() throws Exception {
                                PaymentIntentParams paymentIntentParams =
                                        PaymentIntentParams.createConfirmPaymentIntentWithSourceDataParams(
                                                sourceParams,
                                                clientSecret,
                                                RETURN_URL);
                                return mStripe.confirmPaymentIntentSynchronous(
                                        paymentIntentParams,
                                        PaymentConfiguration.getInstance().getPublishableKey());
                            }
                        });
        Subscription subscription = paymentIntentObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.setMessageResource(R.string.confirming_payment_intent);
                        mProgressDialogController.startProgress();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialogController.finishProgress();
                    }
                })
                .subscribe(
                        new Action1<PaymentIntent>() {
                            @Override
                            public void call(PaymentIntent paymentIntent) {
                                if (paymentIntent != null) {
                                    mPaymentIntentValue.setText(paymentIntent.toString());
                                    Uri authUrl = paymentIntent.getAuthorizationUrl();
                                    if (paymentIntent.getStatus().equals("requires_source_action") && authUrl != null) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, authUrl);
                                        startActivity(browserIntent);
                                    }
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, throwable.toString());
                            }
                        }
                );
        mCompositeSubscription.add(subscription);
    }

}
