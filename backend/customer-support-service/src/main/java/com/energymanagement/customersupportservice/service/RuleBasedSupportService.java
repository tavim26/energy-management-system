package com.energymanagement.customersupportservice.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Answers common questions from a fixed list of rules, without calling the AI
@Service
public class RuleBasedSupportService {

    private record Rule(List<String> keywords, String answer) {
    }

    // Checked in order, so the more specific rules come first
    private static final List<Rule> RULES = List.of(
            new Rule(List.of("limit", "max", "maximum", "threshold"),
                    "Every device has a maximum hourly consumption, in kWh, set by an administrator. "
                            + "When a device goes over this limit within an hour, you receive an alert."),
            new Rule(List.of("alert", "notification", "notif", "warning"),
                    "Overconsumption alerts appear in real time while you are logged in, "
                            + "at most once per device for each hour."),
            new Rule(List.of("consumption", "consum", "energy", "kwh", "usage"),
                    "Each device reports its consumption every 10 minutes, and the values are added up for every hour. "
                            + "You can follow the consumption of your devices on your dashboard."),
            new Rule(List.of("device", "appliance"),
                    "Devices are added and assigned to clients by an administrator. "
                            + "The devices assigned to you are listed on your dashboard."),
            new Rule(List.of("password", "login", "logg", "sign"),
                    "If you cannot log in, check your username and password. "
                            + "Passwords cannot be reset from the application yet, so please contact an administrator."),
            new Rule(List.of("register", "account", "signup"),
                    "Anyone can create a client account from the Register page. "
                            + "Administrator accounts can only be created by an existing administrator."),
            new Rule(List.of("profile", "name", "address"),
                    "Your full name and address can be updated by an administrator."),
            new Rule(List.of("error", "bug", "problem", "broken", "crash"),
                    "Try refreshing the page and logging in again. If the problem persists, contact an administrator."),
            new Rule(List.of("admin", "administrator", "contact"),
                    "For changes to your account or devices, please contact an administrator."),
            new Rule(List.of("help", "support"),
                    "I can answer questions about your account, your devices, energy consumption, limits and alerts."),
            new Rule(List.of("hello", "hi", "hey"),
                    "Hello! Ask me anything about your account, devices, energy consumption or alerts.")
    );

    // Short keywords must match a whole word (so "hi" does not match "history");
    // longer ones also match words that start with them ("notif" matches "notifications")
    private static final int MIN_PREFIX_LENGTH = 4;

    public Optional<String> findAnswer(String message) {
        List<String> words = Arrays.stream(message.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(word -> !word.isEmpty())
                .toList();

        return RULES.stream()
                .filter(rule -> rule.keywords().stream().anyMatch(keyword -> containsKeyword(words, keyword)))
                .map(Rule::answer)
                .findFirst();
    }

    private boolean containsKeyword(List<String> words, String keyword) {
        return words.stream().anyMatch(word -> word.equals(keyword)
                || (keyword.length() >= MIN_PREFIX_LENGTH && word.startsWith(keyword)));
    }
}