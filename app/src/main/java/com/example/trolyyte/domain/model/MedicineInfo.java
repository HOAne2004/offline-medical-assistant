package com.example.trolyyte.domain.model;

public class MedicineInfo {
    private final String name;           // Tên thuốc (VD: "Panadol")
    private final String usage;          // Công dụng ("Giảm đau, hạ sốt")
    private final String dosageGuide;    // Hướng dẫn dùng ("Uống sau khi ăn no")
    private final String sideEffects;    // Tác dụng phụ ("Có thể gây buồn ngủ")
    private final String contraindication; // Chống chỉ định ("Không dùng cho người loét dạ dày")

    public MedicineInfo(String name, String usage, String dosageGuide, String sideEffects, String contraindication) {
        this.name = name;
        this.usage = usage;
        this.dosageGuide = dosageGuide;
        this.sideEffects = sideEffects;
        this.contraindication = contraindication;
    }

    public String getName() { return name; }
    public String getUsage() { return usage; }
    public String getDosageGuide() { return dosageGuide; }
    public String getSideEffects() { return sideEffects; }
    public String getContraindication() { return contraindication; }
}