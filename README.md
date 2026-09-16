<div align="center">
  
# 🎙️ Transcriber Pro
**L'assistente AI personale per le tue note vocali di WhatsApp**

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Gemini_AI-8E75B2?style=for-the-badge&logo=google&logoColor=white)
![Material You](https://img.shields.io/badge/Material_You-3DDC84?style=for-the-badge&logo=android&logoColor=white)

</div>

---

**Transcriber Pro** è un'app Android nativa e leggerissima che si integra direttamente nel menu di condivisione del tuo telefono. Invece di ascoltare lunghi audio su WhatsApp, condividili con Transcriber Pro per ottenere una trascrizione testuale immediata in una comoda finestra a comparsa, senza mai abbandonare la chat.

## ✨ Funzionalità Principali

* 🚀 **Integrazione Trasparente:** Nessun bot, nessun inoltro scomodo. L'app si apre come *Bottom Sheet* nativo in sovraimpressione.
* 🧠 **Motore AI Avanzato:** Sfrutta il modello `gemini-3.5-flash-lite` per trascrizioni ultra-rapide e precise.
* 📌 **Riassunto Intelligente (TL;DR):** Un tasto dedicato per estrapolare automaticamente i 3 punti chiave (bullet points) dagli audio più lunghi.
* 🕒 **Cronologia Locale:** Salva automaticamente le ultime trascrizioni sul dispositivo. Apri l'app dalla Home per rileggerle in qualsiasi momento.
* 🎨 **Design Material You:** L'interfaccia assorbe dinamicamente la palette di colori del tuo sfondo di sistema (Android 12+), garantendo un'estetica curata e nativa.
* 🔒 **Privacy First:** L'audio viene inviato direttamente dal tuo dispositivo alle API, senza passare per server intermedi di terze parti.

---

## 🔑 Come ottenere l'API Key (Gratuita)

Per far funzionare l'app, è necessaria una chiave API di Google Gemini. Google offre un **livello gratuito estremamente generoso** (decine di richieste al minuto), perfetto per l'uso personale quotidiano.

Segui questi 3 semplici passaggi:

1. Visita la console ufficiale: [Google AI Studio](https://aistudio.google.com/).
2. Accedi con il tuo normale account Google.
3. Nel menu a sinistra, clicca su **Get API key** e poi sul bottone blu **Create API key**.
4. Seleziona o crea un nuovo progetto Google Cloud e genera la chiave (una lunga stringa alfanumerica).
5. Copia la chiave. Al primo avvio dell'app sul tuo telefono, ti verrà chiesto di incollarla per salvarla in modo sicuro.

---

## 📦 Installazione

1. Vai nella sezione [Releases](../../releases) di questa repository.
2. Scarica l'ultimo file `app-release.apk` sul tuo smartphone Android.
3. Apri il file e conferma l'installazione (potrebbe esserti richiesto di abilitare l'installazione da "Origini sconosciute" o dal browser/file manager che stai usando).
4. Avvia l'app dalla Home per configurare la tua API Key.

---

## 🗺️ Roadmap e Sviluppi Futuri

* [ ] **Automazione Build:** Implementazione di workflow GitHub Actions per la compilazione automatica dell'APK ad ogni nuova release.
* [ ] **Self-Hosted AI (Local LLM):** Aggiunta di un *toggle* nelle impostazioni per deviare le richieste verso endpoint custom compatibili (es. server vLLM in esecuzione locale su infrastruttura Proxmox) per la massima privacy.
* [ ] **Esportazione:** Funzione per condividere la cronologia completa delle trascrizioni.

---
*Progetto sviluppato per uso personale e rilasciato open-source.*
