# Transcriber Pro per WhatsApp 🎙️

Un'app Android nativa (Material 3) per trascrivere istantaneamente le note vocali di WhatsApp utilizzando le API di Google Gemini, senza mai abbandonare la chat.

## ✨ Funzionalità
* **Integrazione Trasparente:** Si apre come Bottom Sheet direttamente dal menu "Condividi" di WhatsApp.
* **Intelligenza Artificiale:** Sfrutta `gemini-3.5-flash-lite` per trascrizioni rapide e precise.
* **Funzione TL;DR:** Riassume automaticamente le note vocali più lunghe in pratici punti chiave.
* **Cronologia Locale:** Salva automaticamente le ultime trascrizioni in locale sul dispositivo.
* **Material You:** L'interfaccia si adatta dinamicamente ai colori del tema di sistema (Android 12+).

## 🚀 Installazione e Uso
1. Scarica l'ultimo `.apk` dalla sezione **Releases** (o compila il progetto tramite Android Studio).
2. Al primo avvio, inserisci la tua API Key di Google Gemini (che verrà salvata in modo sicuro nelle SharedPreferences locali).
3. Su WhatsApp, tieni premuto su un audio, clicca su "Condividi" e seleziona l'app.

## 🛠️ Stack Tecnologico
* **Linguaggio:** Kotlin
* **UI:** Jetpack Compose
* **AI:** Google Generative AI SDK
