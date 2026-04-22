package com.example.trolyyte.domain.model;

import java.util.List;

public class MedicalIntent {
    public String intent;
    public Entities entities;
    public double confidence;
    public boolean fallback_required;

    public static class Entities {
        public List<String> med_name;
        public List<String> symptom;
        public List<String> body_part;
        public List<String> severity;
        public List<String> datetime;
    }
}