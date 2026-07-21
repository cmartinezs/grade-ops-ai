# Firebase Auth Emulator — used only for local `compose.smoke.yml` runs.
# The Admin SDK (used by api/'s FirebaseAuthAdapter) redirects every Auth call here
# transparently via the FIREBASE_AUTH_EMULATOR_HOST env var — no application code
# needs to know it isn't talking to real Firebase.
FROM node:20-alpine

RUN npm install -g firebase-tools@13.29.1

WORKDIR /emulator
COPY firebase.json .

EXPOSE 9099

# --project is a "demo-*" id: the emulator accepts any such id without needing
# real Firebase project provisioning or credentials.
CMD ["firebase", "emulators:start", "--only", "auth", "--project", "demo-gradeops-smoke"]
