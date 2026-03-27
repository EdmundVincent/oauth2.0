package com.example.sbapp.session;

public final class SessionKeys {
    public static final String OAUTH_STATE = "oauth_state";
    public static final String OIDC_NONCE = "oidc_nonce";
    public static final String PKCE_VERIFIER = "pkce_verifier";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_AT_EPOCH_SECONDS = "expires_at_epoch_seconds";
    public static final String ID_TOKEN = "id_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String LOGOUT_STATE = "logout_state";

    private SessionKeys() {
    }
}
