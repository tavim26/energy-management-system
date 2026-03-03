package com.energymanagement.customersupportservice.service;

import org.springframework.stereotype.Service;

@Service
public class RuleBasedSupportService {

    public String processMessage(String message)
    {

        String lowerMessage = message.toLowerCase();

        // REGULA 1: Parola
        if (lowerMessage.contains("parol") || lowerMessage.contains("password"))
        {
            return "Mergi la login si da click pe 'Ai uitat parola?'. Primesti email cu link de resetare.";
        }

        // REGULA 2: Consum
        if (lowerMessage.contains("consum") || lowerMessage.contains("energie"))
        {
            return "Consumul il vezi in sectiunea 'Monitoring'. Ai acolo grafice pe ore si zile.";
        }

        // REGULA 3: Dispozitive
        if (lowerMessage.contains("dispozitiv") || lowerMessage.contains("device"))
        {
            return "La 'Device Management' poti adauga, modifica sau sterge dispozitive.";
        }

        // REGULA 4: Cont/Profil
        if (lowerMessage.contains("cont") || lowerMessage.contains("profil") || lowerMessage.contains("account"))
        {
            return "Profilul tau e in 'User Profile'. Acolo iti actualizezi datele.";
        }

        // REGULA 5: Program suport
        if (lowerMessage.contains("suport") || lowerMessage.contains("ajutor") || lowerMessage.contains("help"))
        {
            return "Suportul e disponibil Luni-Vineri, 9:00-17:00.";
        }

        // REGULA 6: Administrator
        if (lowerMessage.contains("admin")) {
            return "Pentru probleme complexe, contacteaza administratorul din sectiunea 'Contact Admin'.";
        }

        // REGULA 7: Facturi
        if (lowerMessage.contains("factur") || lowerMessage.contains("plat"))
        {
            return "Facturile se genereaza automat la inceputul lunii. Le gasesti in sectiunea 'Billing'.";
        }

        // REGULA 8: Rapoarte
        if (lowerMessage.contains("raport") || lowerMessage.contains("export"))
        {
            return "Rapoartele se exporta din 'History'. Alegi perioada si formatul (PDF sau CSV).";
        }

        // REGULA 9: Notificari
        if (lowerMessage.contains("notificar") || lowerMessage.contains("alert"))
        {
            return "Notificarile de supraconsum apar automat in timp real cand depasesti limita.";
        }


        // REGULA 10: Erori
        if (lowerMessage.contains("eroare") || lowerMessage.contains("error") || lowerMessage.contains("bug"))
        {
            return "Daca ai probleme: refresh la pagina, verifica internetul, sterge cache-ul. Daca persista, contacteaza suportul.";
        }

        // Nicio regula nu se potriveste
        return null;
    }

    public boolean hasRuleMatch(String message)
    {
        return processMessage(message) != null;
    }
}