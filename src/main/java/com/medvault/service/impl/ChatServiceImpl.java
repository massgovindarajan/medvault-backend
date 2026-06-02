package com.medvault.service.impl;

import com.medvault.dto.request.ChatRequest;
import com.medvault.dto.response.ChatResponse;
import com.medvault.entity.ChatMessage;
import com.medvault.entity.User;
import com.medvault.repository.ChatMessageRepository;
import com.medvault.repository.UserRepository;
import com.medvault.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatRepository;
    private final UserRepository        userRepository;

    @Override
    @Transactional
    public ChatResponse chat(String userEmail, ChatRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        String session = (req.getSessionId() != null && !req.getSessionId().isBlank())
                       ? req.getSessionId()
                       : "session_" + user.getId() + "_" + System.currentTimeMillis();

        // Save user message
        chatRepository.save(ChatMessage.builder()
                .user(user).sender("USER")
                .message(req.getMessage()).sessionId(session).build());

        // Generate bot reply
        String       reply = generateReply(req.getMessage().toLowerCase().trim());
        List<String> sugg  = getSuggestions(req.getMessage().toLowerCase().trim());

        // Save bot reply
        chatRepository.save(ChatMessage.builder()
                .user(user).sender("BOT")
                .message(reply).sessionId(session).build());

        return ChatResponse.builder()
                .reply(reply)
                .sessionId(session)
                .suggestions(sugg)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        return chatRepository.findByUserIdOrderBySentAtDesc(user.getId())
                .stream().map(m -> ChatResponse.builder()
                        .reply(m.getMessage())
                        .sessionId(m.getSessionId())
                        .suggestions(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Rule-based bot ───────────────────────────────────
    private String generateReply(String msg) {
        if (containsAny(msg, "hello","hi","hey","good morning","good evening"))
            return "Hello! 👋 I'm MedVault Assistant. I can help you with appointments, medical records, doctor info, and more. What do you need?";
        if (containsAny(msg, "book appointment","book a doctor","see a doctor","schedule appointment"))
            return "To book an appointment, go to your dashboard → 🩺 Find a Doctor → search for a specialist → pick a date and time. Need help choosing a department?";
        if (containsAny(msg, "my appointment","my booking","upcoming appointment","check appointment"))
            return "You can view all your appointments in your dashboard under 📅 My Appointments. Each appointment shows the status: Pending, Confirmed, or Completed.";
        if (containsAny(msg, "cancel appointment","reschedule"))
            return "To cancel or reschedule, please contact the hospital reception. Receptionist can adjust your appointment slot from their dashboard.";
        if (containsAny(msg, "upload record","add record","upload prescription","upload report"))
            return "Go to your dashboard → 📂 Medical Records → click 'Upload Record'. You can upload prescriptions, test reports, vaccination records, and more.";
        if (containsAny(msg, "my record","my report","my prescription","view record"))
            return "Your medical records are in the 📂 Medical Records section of your dashboard. Sensitive records require you to grant consent before a doctor can view them.";
        if (containsAny(msg, "consent","doctor access","share record"))
            return "When a doctor wants to view a sensitive record, they send you a consent request. You'll see it in your dashboard — you can Approve or Deny access.";
        if (containsAny(msg, "find doctor","list of doctor","doctor available","which doctor"))
            return "You can browse all available doctors in the 🩺 Find a Doctor section. Filter by department like Cardiology, Dermatology, Pediatrics, and more.";
        if (containsAny(msg, "doctor rating","rate doctor","review doctor"))
            return "After your appointment is marked Completed, you can rate your doctor ⭐ from the My Appointments section. Your feedback helps other patients!";
        if (containsAny(msg, "fever","temperature","chills"))
            return "For fever, I recommend seeing a General Medicine doctor. Stay hydrated and rest well. If temperature is above 103°F / 39.4°C, seek urgent care. 🏥";
        if (containsAny(msg, "chest pain","heart pain","breathing","shortness of breath"))
            return "⚠️ Chest pain or breathing difficulty can be serious. Please see a Cardiologist urgently or visit the Emergency department. Don't delay!";
        if (containsAny(msg, "headache","migraine","head pain"))
            return "For persistent headaches or migraines, a Neurologist can help. You can search for one in the 🩺 Find a Doctor section.";
        if (containsAny(msg, "skin","rash","acne","itch"))
            return "Skin concerns are best addressed by a Dermatologist. Book an appointment through Find a Doctor.";
        if (containsAny(msg, "child","baby","infant","pediatric","kids"))
            return "For children's health concerns, our Pediatricians are available. Use Find a Doctor and filter by 'Pediatrics'.";
        if (containsAny(msg, "pregnancy","pregnant","gynec","women health"))
            return "Our Gynecology department handles pregnancy and women's health. Book an appointment with a Gynecologist via Find a Doctor.";
        if (containsAny(msg, "forgot password","reset password","change password"))
            return "On the login page, use 'Reset Password'. If it's your first login, enter the temporary password sent after admin approval, then set a new one.";
        if (containsAny(msg, "account","register","sign up","not approved"))
            return "After registering, your account goes through L1 (details) and L2 (documents) admin verification. Once both approve, your login credentials are automatically generated.";
        if (containsAny(msg, "emergency","ambulance","urgent","accident"))
            return "🚨 For a medical emergency, call 108 (ambulance) or 112 immediately. Go to the nearest Emergency department.";
        if (containsAny(msg, "help","what can you do","options","menu"))
            return "I can help with: 📅 Booking appointments · 📂 Medical records · 👨‍⚕️ Finding doctors · 🔑 Account issues · 🏥 Department info · 💊 General health guidance. Just ask!";
        if (containsAny(msg, "thank","thanks","bye","goodbye","ok thanks"))
            return "You're welcome! 😊 If you need anything else, I'm here. Take care and stay healthy! 💙";
        return "I'm not sure I understood that. Could you rephrase? You can ask me about booking appointments, your medical records, finding a doctor, or account-related questions. 🤔";
    }

    private List<String> getSuggestions(String msg) {
        if (containsAny(msg, "hello","hi","hey","help"))
            return Arrays.asList("Book appointment", "Find a doctor", "My appointments", "Upload records");
        if (containsAny(msg, "appointment","book","doctor"))
            return Arrays.asList("View my appointments", "Find a specialist", "What departments are available?");
        if (containsAny(msg, "record","report","prescription"))
            return Arrays.asList("How to upload records", "What is consent?", "Who can see my records?");
        return Arrays.asList("Book appointment", "My records", "Find a doctor", "Help");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }
}