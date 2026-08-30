# 🚀 Streamlit Deployment Guide for StudyMate AI

This repository is configured and ready for 1-click deployment on **Streamlit Community Cloud**, **Hugging Face Spaces**, **Render**, **Railway**, or **Docker**.

---

## 🌟 Option 1: Streamlit Community Cloud (Recommended)

1. Fork or push this repository to your **GitHub** account.
2. Visit [share.streamlit.io](https://share.streamlit.io/) and log in with GitHub.
3. Click **New app**.
4. Configure your app:
   - **Repository**: `your-username/StudyMate`
   - **Branch**: `main`
   - **Main file path**: `app.py` (or `streamlit_app.py`)
5. Click **Advanced settings... > Secrets** and paste your credentials:
   ```toml
   GEMINI_API_KEY = "your_gemini_api_key_here"
   SUPABASE_URL = "https://your-project.supabase.co"
   SUPABASE_KEY = "your-supabase-anon-key"
   ```
6. Click **Deploy!**

---

## 🐳 Option 2: Docker / Container Deployment

Build and run the container locally or on any cloud server (e.g. AWS ECS, GCP Cloud Run, Render):

```bash
# Build image
docker build -t studymate-streamlit .

# Run container
docker run -p 8501:8501 -e GEMINI_API_KEY="your_api_key" studymate-streamlit
```

Open your browser at `http://localhost:8501`.

---

## 💻 Option 3: Local Development

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Run Streamlit
streamlit run app.py
```

---

## ⚙️ Configuration & Secrets

| Secret Key | Description | Optional? |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API Key for AI Tutor, Flashcard & Quiz generation | Optional (defaults to smart demo mode) |
| `SUPABASE_URL` | Supabase Cloud project URL | Optional |
| `SUPABASE_KEY` | Supabase Public Anon Key | Optional |
