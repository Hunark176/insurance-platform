package test;


import java.util.*;
        import java.util.stream.*;
        import java.math.BigDecimal;


public class StreamsUebung {

    // Test-Daten — brauchen wir immer wieder
    record Vertrag(Long id, String kunde,
                   String typ, BigDecimal betrag,
                   boolean aktiv) {}   static List<Vertrag> vertraege = List.of(
            new Vertrag(1L, "Müller",  "KFZ",         new BigDecimal("450.00"), true),
            new Vertrag(2L, "Müller",  "Haftpflicht", new BigDecimal("120.00"), true),
            new Vertrag(3L, "Schmidt", "KFZ",         new BigDecimal("380.00"), false),
            new Vertrag(4L, "Schmidt", "Hausrat",     new BigDecimal("200.00"), true),
            new Vertrag(5L, "Wagner",  "KFZ",         new BigDecimal("520.00"), true),
            new Vertrag(6L, "Wagner",  "Rechtsschutz",new BigDecimal("180.00"), true),
            new Vertrag(7L, "Meyer",   "KFZ",         new BigDecimal("410.00"), false),
            new Vertrag(8L, "Meyer",   "Haftpflicht", new BigDecimal("95.00"),  true)
    );





    public static void main(String[] args) {
        long anzahl = vertraege.stream()
                .filter(vertrag -> vertrag.kunde() != null)
                .count();

        List<String> kundenListe = vertraege.stream()
                .filter(Vertrag::aktiv)
                .map(Vertrag::kunde)
                .distinct()
                .toList();
        System.out.println("Kunden: " + kundenListe);

        //System.out.println("Verträge mit Kunden: " + anzahl);

        List<Vertrag> hoheVertraege = vertraege.stream()
                .filter(Vertrag::aktiv)
                .filter(vertrag ->
                        vertrag.betrag()
                                .compareTo(new BigDecimal("400.00")) >= 0)
                .toList();

        //System.out.println(hoheVertraege);

        BigDecimal dist = vertraege.stream()
                .filter(Vertrag::aktiv)
                .map(Vertrag::betrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //System.out.println(dist);

        Vertrag vertrag = vertraege.stream()
                .filter(v -> v.kunde().equals("Wagner"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));
        System.out.println(vertrag);
       // name.ifPresent(System.out::println);

        Map<String, List<Vertrag>> nachTyp = vertraege.stream()
                .collect(Collectors.groupingBy(Vertrag::typ));

        nachTyp.forEach((typ, liste) ->
                System.out.println(typ + " → " + liste));
    }
}