package com.enseval.gcmuser.API;

/**Class untuk client apollo graphql*/
public class ApolloClient {
    //    private String BASE_URL = "http://10.163.205.55:4000/graphql";
//    private String BASE_URL = "https://moshealth.co.id:447/graphql";
//    private String BASE_URL = "https://moshealth.co.id:4001/graphql";
//    private ApolloClient apolloClient;
//    private Context context;
//
//    public ApolloClient(Context context){
//        this.context = context;
//    }
//
////    public static ApolloClient getMyApolloClient(){
////        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
////        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
////
////        OkHttpClient okHttpClient = new OkHttpClient.Builder()
////                .addInterceptor(loggingInterceptor)
////                .build();
////
////        myApolloClient = ApolloClient.builder()
////                .serverUrl(BASE_URL)
////                .okHttpClient(okHttpClient)
////                .build();
////
////        return myApolloClient;
////    }
//
//    public ApolloClient getMyApolloClient(){
//        //
//        // mengecek apkah sudah login atau blmm
//        // 0 - for private mode`
//        String orgid = "";
//        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0);
//        SharedPreferences.Editor editor = pref.edit();
//        orgid = pref.getString("orgid_key", ""); // getting String
//        //
//
//        InputStream inputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.moshealth_co_id));
//        try {
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//            Certificate certificate = certificateFactory.generateCertificate(inputStream);
//
//            // Create a KeyStore containing our trusted CAs
//            String keyStoreType = KeyStore.getDefaultType();
//            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//            keyStore.load(null, null);
//            keyStore.setCertificateEntry("ca", certificate);
//
//            // Create a TrustManager that trusts the CAs in our KeyStore
//            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
//            trustManagerFactory.init(keyStore);
//
//            // Create an SSLContext that uses our TrustManager
//            SSLContext context = SSLContext.getInstance("TLS");
//            context.init(null, trustManagerFactory.getTrustManagers(), null);
//
//            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//            final X509TrustManager x509TrustManager = (X509TrustManager)trustManagers[0];
//
//            // Tell the okhttp to use a SocketFactory from our SSLContext
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            final String finalOrgid = orgid;
//
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(new Interceptor() {
//                        @Override
//                        public Response intercept(Chain chain) throws IOException {
//                            Request original = chain.request();
//
//                            Request request = original.newBuilder()
//                                    .header("Authorization", finalOrgid)
//                                    .method(original.method(), original.body())
//                                    .build();
//
//                            return chain.proceed(request);
//                        }
//                    })
//                    .connectTimeout(1, TimeUnit.MINUTES)
//                    .writeTimeout(2, TimeUnit.MINUTES)
//                    .readTimeout(1, TimeUnit.MINUTES)
//                    .addInterceptor(httpLoggingInterceptor)
//                    .sslSocketFactory(context.getSocketFactory(), x509TrustManager)
//                    .build();
//
//            apolloClient = ApolloClient.builder()
//                    .serverUrl(BASE_URL)
//                    .okHttpClient(okHttpClient)
//                    .build();
//
//            return apolloClient;
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                inputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
}
