Kluer: AI-Integrated Personal Memory Assistant

Kluer is an AI-powered Android application designed to act as a personal memory assistant. The app leverages intelligent retrieval and user-friendly features to help users manage and recall memories efficiently.

Features User Authentication

Users can register and log in with their email and password.
Secure authentication is implemented using Firebase Authentication.
Profile Management

Users can upload a profile image and enter personal details such as name, gender, and date of birth.
Intelligent Input Handling

Text-based inputs: Users can send queries or information as text, which is forwarded to a Retrieval-Augmented Generation (RAG) model hosted on a remote server via API.
Image and audio input functionality is also implemented within the application, with current RAG capabilities supporting text input. Support for image and audio data will be added in future updates.
AI-Powered Query Response

Users can ask questions, and the queries are processed by the RAG model, which retrieves and displays relevant answers.
Automated Location Tracking

The app logs the user’s location every 10 minutes, storing each entry (with timestamp and date) in an internal file.
Each day at 11:50 PM, the daily location log is summarized and sent to the RAG model as a regular message.
This enables users to later query their whereabouts for specific times and dates through the AI assistant.
How it Works

Register/Login: Create an account using your email and password.
Set Up Profile: Add personal details and a profile image.
Input Data: Provide information or questions as text; image and audio input are supported with forthcoming RAG integration.
Receive Answers: Submit queries to the built-in AI and get instant, context-aware responses.
Location Log: Let the app automatically store and summarize daily location data for effortless recall.
Kluer aims to be your reliable digital memory companion, integrating advanced AI capabilities with privacy and convenience.
