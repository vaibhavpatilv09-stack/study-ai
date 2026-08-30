import os
import json
import time
from datetime import datetime
import streamlit as st
import pandas as pd

# Try loading optional integrations safely
try:
    from dotenv import load_dotenv
    load_dotenv()
except Exception:
    pass

try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except Exception:
    GEMINI_AVAILABLE = False

try:
    from supabase import create_client, Client
    SUPABASE_AVAILABLE = True
except Exception:
    SUPABASE_AVAILABLE = False

# Page Configuration
st.set_page_config(
    page_title="StudyMate AI - Learning Mastery Platform",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom Styling
st.markdown("""
<style>
    .main-title {
        font-size: 2.2rem;
        font-weight: 800;
        background: linear-gradient(135deg, #6366F1 0%, #A855F7 50%, #EC4899 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: 0.2rem;
    }
    .sub-title {
        color: #94A3B8;
        font-size: 1.05rem;
        margin-bottom: 1.5rem;
    }
    .study-card {
        background-color: #1E293B;
        border-radius: 12px;
        padding: 1.25rem;
        border: 1px solid #334155;
        margin-bottom: 1rem;
        transition: transform 0.2s ease, border-color 0.2s ease;
    }
    .study-card:hover {
        border-color: #6366F1;
    }
    .metric-badge {
        display: inline-block;
        padding: 0.25rem 0.65rem;
        border-radius: 9999px;
        font-size: 0.8rem;
        font-weight: 600;
        background-color: #312E81;
        color: #C7D2FE;
        margin-right: 0.5rem;
    }
    .badge-xp {
        background-color: #701A75;
        color: #F5D0FE;
    }
    .badge-success {
        background-color: #064E3B;
        color: #A7F3D0;
    }
    .flashcard-box {
        background: linear-gradient(145deg, #1E293B, #0F172A);
        border: 2px solid #4F46E5;
        border-radius: 16px;
        padding: 2rem;
        min-height: 180px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        margin: 1rem 0;
    }
</style>
""", unsafe_allow_html=True)

# Initialize Session State
if "messages" not in st.session_state:
    st.session_state.messages = [
        {"role": "assistant", "content": "👋 Hi there! I'm **StudyMate AI**, your personal study companion. What subject or exam are you preparing for today?"}
    ]

if "flashcards" not in st.session_state:
    st.session_state.flashcards = [
        {"front": "What is Amortized Analysis in Algorithm Design?", "back": "A method of analyzing algorithms that considers the average running time per operation over a worst-case sequence of operations (e.g. dynamic array doubling).", "category": "Computer Science"},
        {"front": "What is the role of Mitochondria in eukaryotic cells?", "back": "Known as the powerhouse of the cell, mitochondria generate most of the chemical energy needed to power the cell's biochemical reactions through ATP synthesis via oxidative phosphorylation.", "category": "Biology"},
        {"front": "What is Bayes' Theorem?", "back": "A formula that describes the probability of an event, based on prior knowledge of conditions that might be related: P(A|B) = [P(B|A) * P(A)] / P(B).", "category": "Mathematics"}
    ]

if "quiz_data" not in st.session_state:
    st.session_state.quiz_data = []

if "quiz_answers" not in st.session_state:
    st.session_state.quiz_answers = {}

if "quiz_submitted" not in st.session_state:
    st.session_state.quiz_submitted = False

if "user_xp" not in st.session_state:
    st.session_state.user_xp = 420

if "study_streak" not in st.session_state:
    st.session_state.study_streak = 7

if "community_posts" not in st.session_state:
    st.session_state.community_posts = [
        {
            "id": 1,
            "author": "Maya Lin",
            "title": "Intuitive explanation of Backpropagation and Gradient Descent",
            "category": "Machine Learning",
            "content": "Think of gradient descent as walking down a foggy mountain trying to find the lowest valley. The gradient is the slope beneath your feet, and learning rate is your step size!",
            "likes": 24,
            "comments": 6,
            "time": "2 hours ago"
        },
        {
            "id": 2,
            "author": "Liam Vance",
            "title": "Organic Chemistry Reaction Mnemonics that saved my exam",
            "category": "Chemistry",
            "content": "Remember SN1 vs SN2: SN1 likes Tertiary carbons and Polar Protic solvents (1 step for carbocation formation, 1 step for attack). SN2 does backside attack on Methyl/Primary carbons!",
            "likes": 39,
            "comments": 11,
            "time": "5 hours ago"
        }
    ]

# Setup API Keys
gemini_key = st.secrets.get("GEMINI_API_KEY", os.getenv("GEMINI_API_KEY", ""))
supabase_url = st.secrets.get("SUPABASE_URL", os.getenv("SUPABASE_URL", ""))
supabase_key = st.secrets.get("SUPABASE_KEY", os.getenv("SUPABASE_KEY", ""))

# Sidebar
with st.sidebar:
    st.image("https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500&auto=format&fit=crop&q=80", use_container_width=True)
    st.markdown("## 🎓 **StudyMate AI**")
    st.markdown(f"<span class='metric-badge badge-xp'>⚡ {st.session_state.user_xp} XP</span><span class='metric-badge'>🔥 {st.session_state.study_streak} Day Streak</span>", unsafe_allow_html=True)
    st.markdown("---")

    nav_option = st.radio(
        "Navigation",
        [
            "💬 AI Study Tutor",
            "⚡ Flashcards & Decks",
            "📝 Smart Quiz Generator",
            "🗺️ Concept Roadmap Explorer",
            "👥 Community Discussions",
            "⚙️ Cloud & Settings"
        ],
        index=0
    )

    st.markdown("---")
    st.markdown("### 🔑 API Configuration")
    user_api_key = st.text_input("Gemini API Key", value=gemini_key, type="password", help="Enter your Gemini API key or configure in Streamlit secrets.")
    active_gemini_key = user_api_key if user_api_key else gemini_key

    if active_gemini_key:
        st.success("✅ Gemini AI Connected")
    else:
        st.info("💡 Running in local smart demo mode. Add an API key for live generative answers.")

# Helper Gemini Generator
def call_gemini(prompt: str, system_instruction: str = "You are StudyMate AI, an expert academic tutor."):
    if not active_gemini_key or not GEMINI_AVAILABLE:
        # High quality fallback answers
        time.sleep(0.6)
        if "flashcard" in prompt.lower():
            return json.dumps([
                {"front": "Key Concept: " + prompt[:30], "back": "Comprehensive explanation with practical example.", "category": "Study Deck"}
            ])
        return f"*(Demo Response - Add your Gemini API Key in the sidebar for full live generation)*\n\nHere is a structured explanation based on your query:\n\n1. **Core Concept**: Fundamental definition and context.\n2. **Key Mechanism**: How this applies to your subject.\n3. **Exam Tip**: Remember formulas, trade-offs, and key vocabulary!"

    try:
        genai.configure(api_key=active_gemini_key)
        model = genai.GenerativeModel(
            model_name="gemini-1.5-flash",
            system_instruction=system_instruction
        )
        response = model.generate_content(prompt)
        return response.text
    except Exception as e:
        return f"Error communicating with Gemini API: {str(e)}"


# -------------------------------------------------------------
# TAB 1: AI Study Tutor
# -------------------------------------------------------------
if nav_option == "💬 AI Study Tutor":
    st.markdown("<div class='main-title'>💬 AI Study Tutor & Mentor</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Ask any question, break down difficult proofs, solve problems step-by-step, or get exam prep coaching.</div>", unsafe_allow_html=True)

    col1, col2, col3 = st.columns([2, 1, 1])
    with col1:
        tutor_mode = st.selectbox(
            "Tutor Persona",
            ["Socratic Guide (Ask guiding questions)", "Direct Explainer (Clear & concise)", "Exam Coach (Past paper style & mnemonics)", "Code & Algorithm Specialist"]
        )
    with col2:
        subject_tag = st.selectbox("Subject Focus", ["General STEM", "Computer Science", "Biology & Medicine", "Physics & Math", "Humanities & Economics"])
    with col3:
        if st.button("🧹 Clear Chat", use_container_width=True):
            st.session_state.messages = [
                {"role": "assistant", "content": "👋 Chat reset. What would you like to learn next?"}
            ]
            st.rerun()

    # Display chat history
    for msg in st.session_state.messages:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])

    # Chat Input
    if user_prompt := st.chat_input("Ask a concept, homework question, or paste text to summarize..."):
        st.session_state.messages.append({"role": "user", "content": user_prompt})
        with st.chat_message("user"):
            st.markdown(user_prompt)

        with st.chat_message("assistant"):
            message_placeholder = st.empty()
            message_placeholder.markdown("🧠 *Analyzing concept and generating explanation...*")

            system_prompt = f"You are StudyMate AI, acting as a {tutor_mode}. Focus domain: {subject_tag}. Use clear markdown, bold terms, code blocks when relevant, and concise step-by-step points."
            ai_reply = call_gemini(user_prompt, system_prompt)
            message_placeholder.markdown(ai_reply)

        st.session_state.messages.append({"role": "assistant", "content": ai_reply})
        st.session_state.user_xp += 15


# -------------------------------------------------------------
# TAB 2: Flashcards & Decks
# -------------------------------------------------------------
elif nav_option == "⚡ Flashcards & Decks":
    st.markdown("<div class='main-title'>⚡ AI Flashcard & Deck Engine</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Generate active recall study decks from any topic, textbook excerpt, or lecture notes.</div>", unsafe_allow_html=True)

    tab_review, tab_generate = st.tabs(["📖 Study Active Deck", "✨ Generate New Deck"])

    with tab_review:
        if not st.session_state.flashcards:
            st.info("No flashcards found. Generate a new deck to get started!")
        else:
            col_deck, col_stats = st.columns([3, 1])
            with col_deck:
                st.markdown(f"**Total Cards in Deck**: {len(st.session_state.flashcards)}")
                card_idx = st.slider("Select Card", 1, len(st.session_state.flashcards), 1) - 1
                curr_card = st.session_state.flashcards[card_idx]

                show_back = st.toggle("🔄 Flip Card (Reveal Answer)", key=f"flip_{card_idx}")

                if show_back:
                    st.markdown(f"""
                    <div class='flashcard-box' style='border-color: #10B981;'>
                        <span class='metric-badge badge-success'>{curr_card.get('category', 'General')}</span>
                        <h3 style='margin-top: 1rem; color: #A7F3D0;'>💡 Answer / Definition</h3>
                        <p style='font-size: 1.15rem; color: #F1F5F9;'>{curr_card['back']}</p>
                    </div>
                    """, unsafe_allow_html=True)
                else:
                    st.markdown(f"""
                    <div class='flashcard-box'>
                        <span class='metric-badge'>{curr_card.get('category', 'General')}</span>
                        <h3 style='margin-top: 1rem; color: #E0E7FF;'>❓ Question / Concept</h3>
                        <p style='font-size: 1.25rem; font-weight: 600; color: #FFFFFF;'>{curr_card['front']}</p>
                    </div>
                    """, unsafe_allow_html=True)

                btn1, btn2, btn3 = st.columns(3)
                with btn1:
                    if st.button("❌ Needs Review", use_container_width=True):
                        st.toast("Card marked for spaced repetition review!")
                with btn2:
                    if st.button("✅ Got It Right (+10 XP)", use_container_width=True):
                        st.session_state.user_xp += 10
                        st.toast("Great job! +10 XP awarded 🎉")
                with btn3:
                    if st.button("🗑️ Delete Card", use_container_width=True):
                        st.session_state.flashcards.pop(card_idx)
                        st.rerun()

            with col_stats:
                st.markdown("### 📊 Deck Progress")
                st.metric("Mastery Level", "78%", "+12% this week")
                st.metric("Total XP Earned", f"{st.session_state.user_xp} XP")
                st.metric("Daily Goal", "15 / 20 Cards")

    with tab_generate:
        st.markdown("### 🪄 Create Flashcards with Gemini AI")
        gen_topic = st.text_input("Enter Subject / Topic", "Neurotransmitters and Brain Function")
        num_cards = st.slider("Number of Flashcards", 3, 10, 5)
        raw_notes = st.text_area("Optional: Paste Notes, Syllabus, or Article Text", "", height=120)

        if st.button("🚀 Generate Flashcard Deck", type="primary", use_container_width=True):
            with st.spinner("Generating flashcards with Gemini..."):
                prompt = f"""
                Generate {num_cards} high-yield academic flashcards for the topic: '{gen_topic}'.
                Additional notes: {raw_notes}
                
                Respond ONLY with a valid JSON array of objects with keys "front", "back", and "category".
                Example:
                [
                  {{"front": "What is...", "back": "The answer is...", "category": "{gen_topic}"}}
                ]
                """
                raw_json = call_gemini(prompt, "You are a JSON-only flashcard generator.")
                try:
                    # Clean markdown codeblocks if present
                    clean_json = raw_json.strip()
                    if clean_json.startswith("```json"):
                        clean_json = clean_json[7:]
                    if clean_json.startswith("```"):
                        clean_json = clean_json[3:]
                    if clean_json.endswith("```"):
                        clean_json = clean_json[:-3]
                    parsed = json.loads(clean_json.strip())
                    if isinstance(parsed, list):
                        st.session_state.flashcards.extend(parsed)
                        st.session_state.user_xp += 25
                        st.success(f"🎉 Successfully created {len(parsed)} flashcards! +25 XP")
                        st.rerun()
                except Exception:
                    # Fallback standard creation
                    st.session_state.flashcards.append({
                        "front": f"Key mechanism in {gen_topic}",
                        "back": f"Fundamental principle and application regarding {gen_topic}.",
                        "category": gen_topic
                    })
                    st.success("Created flashcards in deck!")


# -------------------------------------------------------------
# TAB 3: Smart Quiz Generator
# -------------------------------------------------------------
elif nav_option == "📝 Smart Quiz Generator":
    st.markdown("<div class='main-title'>📝 Smart Quiz Generator & Exam Sim</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Test your knowledge with customized multiple-choice questions, instant explanations, and scoring.</div>", unsafe_allow_html=True)

    with st.expander("⚙️ Quiz Parameters", expanded=(len(st.session_state.quiz_data) == 0)):
        q_topic = st.text_input("Quiz Topic", "Distributed Systems & Cloud Computing")
        q_difficulty = st.select_slider("Difficulty Level", ["Beginner", "Intermediate", "Advanced", "PhD / Olympiad"])
        q_count = st.slider("Number of Questions", 3, 8, 4)

        if st.button("✨ Create Quiz with Gemini", type="primary"):
            with st.spinner("Formulating multiple-choice questions..."):
                prompt = f"""
                Generate a {q_count}-question multiple choice quiz on '{q_topic}' at {q_difficulty} difficulty.
                Respond ONLY with a valid JSON array of objects with keys:
                - "question" (string)
                - "options" (array of 4 strings)
                - "answer" (string matching exactly one of the options)
                - "explanation" (detailed explanation why this is correct)
                """
                raw_json = call_gemini(prompt, "You are a JSON-only academic exam creator.")
                try:
                    clean_json = raw_json.strip()
                    if clean_json.startswith("```json"): clean_json = clean_json[7:]
                    if clean_json.startswith("```"): clean_json = clean_json[3:]
                    if clean_json.endswith("```"): clean_json = clean_json[:-3]
                    parsed = json.loads(clean_json.strip())
                    if isinstance(parsed, list):
                        st.session_state.quiz_data = parsed
                        st.session_state.quiz_answers = {}
                        st.session_state.quiz_submitted = False
                        st.success("Quiz generated successfully!")
                        st.rerun()
                except Exception:
                    st.session_state.quiz_data = [
                        {
                            "question": f"What is the primary constraint in {q_topic}?",
                            "options": ["Scalability and Consistency trade-off", "Single point of hardware failure", "Memory bandwidth", "Network encryption"],
                            "answer": "Scalability and Consistency trade-off",
                            "explanation": "According to the CAP theorem and distributed systems fundamentals, balancing consistency, availability, and partition tolerance is essential."
                        }
                    ]
                    st.session_state.quiz_answers = {}
                    st.session_state.quiz_submitted = False

    if st.session_state.quiz_data:
        st.markdown("---")
        score = 0
        total = len(st.session_state.quiz_data)

        for i, q in enumerate(st.session_state.quiz_data):
            st.markdown(f"### Q{i+1}: {q['question']}")
            selected = st.radio(
                f"Choose answer for Question {i+1}:",
                q["options"],
                key=f"quiz_opt_{i}",
                index=None if i not in st.session_state.quiz_answers else q["options"].index(st.session_state.quiz_answers[i])
            )
            if selected:
                st.session_state.quiz_answers[i] = selected

            if st.session_state.quiz_submitted:
                user_ans = st.session_state.quiz_answers.get(i, None)
                correct_ans = q["answer"]
                if user_ans == correct_ans:
                    score += 1
                    st.success(f"✅ **Correct!** ({correct_ans})\n\n*{q['explanation']}*")
                else:
                    st.error(f"❌ **Incorrect.** You chose: {user_ans}. **Correct answer**: {correct_ans}\n\n*{q['explanation']}*")
            st.markdown("---")

        if not st.session_state.quiz_submitted:
            if st.button("📤 Submit Quiz & Calculate Score", type="primary", use_container_width=True):
                st.session_state.quiz_submitted = True
                st.rerun()
        else:
            percentage = int((score / total) * 100)
            earned_xp = score * 20
            st.session_state.user_xp += earned_xp
            st.balloons()
            st.markdown(f"""
            <div class='study-card' style='text-align: center; border-color: #6366F1;'>
                <h2>🏆 Quiz Results: {score}/{total} ({percentage}%)</h2>
                <p>Earned <b>+{earned_xp} XP</b> for completing this test!</p>
            </div>
            """, unsafe_allow_html=True)
            if st.button("🔄 Retake or Create New Quiz", use_container_width=True):
                st.session_state.quiz_submitted = False
                st.session_state.quiz_data = []
                st.rerun()


# -------------------------------------------------------------
# TAB 4: Concept Roadmap Explorer
# -------------------------------------------------------------
elif nav_option == "🗺️ Concept Roadmap Explorer":
    st.markdown("<div class='main-title'>🗺️ Concept Mindmap & Roadmap</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Break down any complex subject into structured mastery steps and visual milestones.</div>", unsafe_allow_html=True)

    roadmap_query = st.text_input("Enter Target Skill or Subject", "Full-Stack AI Application Development")
    if st.button("🗺️ Generate Learning Roadmap", type="primary"):
        with st.spinner("Drafting structured curriculum..."):
            prompt = f"Create a comprehensive 4-stage learning roadmap for mastering: '{roadmap_query}'. Provide stages, key concepts to learn in each stage, practical project ideas, and common pitfalls."
            roadmap_md = call_gemini(prompt, "You are a master curriculum architect.")
            st.markdown(roadmap_md)


# -------------------------------------------------------------
# TAB 5: Community Discussions
# -------------------------------------------------------------
elif nav_option == "👥 Community Discussions":
    st.markdown("<div class='main-title'>👥 Community Study Hub</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Share explanations, exchange study hacks, and collaborate with fellow learners.</div>", unsafe_allow_html=True)

    with st.expander("✍️ Create a New Discussion Post"):
        post_title = st.text_input("Post Title")
        post_cat = st.selectbox("Category", ["Computer Science", "Mathematics", "Biology", "Chemistry", "Study Tips"])
        post_body = st.text_area("Discussion Content")
        if st.button("🚀 Publish Post", type="primary"):
            if post_title and post_body:
                new_post = {
                    "id": len(st.session_state.community_posts) + 1,
                    "author": "You (Scholar)",
                    "title": post_title,
                    "category": post_cat,
                    "content": post_body,
                    "likes": 1,
                    "comments": 0,
                    "time": "Just now"
                }
                st.session_state.community_posts.insert(0, new_post)
                st.session_state.user_xp += 30
                st.success("Post published! +30 XP")
                st.rerun()

    st.markdown("### 📌 Active Community Discussions")
    for post in st.session_state.community_posts:
        with st.container():
            st.markdown(f"""
            <div class='study-card'>
                <div style='display: flex; justify-content: space-between; align-items: center;'>
                    <span class='metric-badge'>{post['category']}</span>
                    <span style='color: #64748B; font-size: 0.85rem;'>{post['time']} • by <b>{post['author']}</b></span>
                </div>
                <h4 style='margin: 0.75rem 0 0.5rem 0; color: #FFFFFF;'>{post['title']}</h4>
                <p style='color: #CBD5E1; font-size: 0.95rem;'>{post['content']}</p>
                <div style='margin-top: 0.5rem; color: #94A3B8; font-size: 0.85rem;'>
                    ❤️ {post['likes']} Likes &nbsp;•&nbsp; 💬 {post['comments']} Comments
                </div>
            </div>
            """, unsafe_allow_html=True)


# -------------------------------------------------------------
# TAB 6: Cloud & Settings
# -------------------------------------------------------------
elif nav_option == "⚙️ Cloud & Settings":
    st.markdown("<div class='main-title'>⚙️ Cloud & Deployment Settings</div>", unsafe_allow_html=True)
    st.markdown("<div class='sub-title'>Streamlit Cloud deployment diagnostics, environment keys, and database connections.</div>", unsafe_allow_html=True)

    col1, col2 = st.columns(2)
    with col1:
        st.markdown("### ☁️ Deployment Environment Checklist")
        st.markdown(f"- **Streamlit Version**: `{st.__version__}`")
        st.markdown(f"- **Gemini Library**: `{'✅ Installed' if GEMINI_AVAILABLE else '❌ Missing'}`")
        st.markdown(f"- **Supabase SDK**: `{'✅ Installed' if SUPABASE_AVAILABLE else '❌ Missing'}`")
        st.markdown(f"- **Gemini API Key**: `{'✅ Configured' if active_gemini_key else '⚠️ Not Set (Running Demo Mode)'}`")
        st.markdown(f"- **Supabase URL**: `{'✅ Configured' if supabase_url else '⚠️ Not Set'}`")

    with col2:
        st.markdown("### 🚀 Streamlit Cloud Deployment Guide")
        st.markdown("""
        To deploy this app on **Streamlit Community Cloud** (share.streamlit.io):
        1. Push this repository to GitHub.
        2. Go to [share.streamlit.io](https://share.streamlit.io) and select **New App**.
        3. Set Main file path: `app.py` or `streamlit_app.py`.
        4. In **App Settings > Secrets**, paste:
        ```toml
        GEMINI_API_KEY = "your_gemini_api_key_here"
        SUPABASE_URL = "https://your-project.supabase.co"
        SUPABASE_KEY = "your-supabase-anon-key"
        ```
        5. Click **Deploy**! 🚀
        """)

    st.markdown("---")
    st.info("StudyMate AI is ready for instant deployment on Streamlit Cloud, Render, Hugging Face Spaces, or Docker.")
