const { CognitoJwtVerifier } = require("aws-jwt-verify");

const verifier = CognitoJwtVerifier.create({
    userPoolId: "eu-north-1_regp2NIhU", // Podaj ID swojej grupy użytkowników Cognito
    tokenUse: "access",      // Sprawdzamy access token
    clientId: ["69t98uivj2p9b9i9dn7q8obfqb", "pis58beaainieqgqccc2me8pe"] // Podaj ID aplikacji klienta
});

module.exports = verifier;
