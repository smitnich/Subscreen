package com.subscreen;

/**
 * Created by Nick on 4/13/2016.
 */
public class Languages {
    public static class Language {
        Language(String _shortName, String _fullName) {
            fullName = _fullName;
            shortName = _shortName;
        }
        public String toString() {
            return fullName;
        }
        // The human readable version of the language name, ie "English"
        String fullName;
        // The value used by the API to determine language, ie "eng"
        String shortName;
    }
    public static Language[] allLanguages = {
            new Language("all", "ALL"),
            new Language("eng", "English"),
            new Language("afr", "Afrikaans"),
            new Language("alb", "Albanian"),
            new Language("ara", "Arabic"),
            new Language("arm", "Armenian"),
            new Language("ast", "Asturian"),
            new Language("baq", "Basque"),
            new Language("bel", "Belarusian"),
            new Language("ben", "Bengali"),
            new Language("bos", "Bosnian"),
            new Language("bre", "Breton"),
            new Language("bul", "Bulgarian"),
            new Language("bur", "Burmese"),
            new Language("cat", "Catalan"),
            new Language("chi", "Chinese(simplified)"),
            new Language("zht", "Chinese (traditional)"),
            new Language("zhe", "Chinese (bilingual)"),
            new Language("hrv", "Croatian"),
            new Language("cze", "Czech"),
            new Language("dan", "Danish"),
            new Language("dut", "Dutch"),
            new Language("epo", "Esperanto"),
            new Language("est", "Estonian"),
            new Language("fin", "Finnish"),
            new Language("fre", "French"),
            new Language("glg", "Galician"),
            new Language("geo", "Georgian"),
            new Language("ger", "German"),
            new Language("ell", "Greek"),
            new Language("heb", "Hebrew"),
            new Language("hin", "Hindi"),
            new Language("hun", "Hungarian"),
            new Language("ice", "Icelandic"),
            new Language("ind", "Indonesian"),
            new Language("ita", "Italian"),
            new Language("jpn", "Japanese"),
            new Language("kaz", "Kazakh"),
            new Language("khm", "Khmer"),
            new Language("kor", "Korean"),
            new Language("lav", "Latvian"),
            new Language("lit", "Lithuanian"),
            new Language("ltz", "Luxembourgish"),
            new Language("mac", "Macedonian"),
            new Language("may", "Malay"),
            new Language("mal", "Malayalam"),
            new Language("mni", "Manipuri"),
            new Language("mon", "Mongolian"),
            new Language("mne", "Montenegrin"),
            new Language("nor", "Norwegian"),
            new Language("oci", "Occitan"),
            new Language("per", "Persian"),
            new Language("pol", "Polish"),
            new Language("por", "Portuguese"),
            new Language("pob", "Portuguese (BR)"),
            new Language("rum", "Romanian"),
            new Language("rus", "Russian"),
            new Language("scc", "Serbian"),
            new Language("sin", "Sinhalese"),
            new Language("slo", "Slovak"),
            new Language("slv", "Slovenian"),
            new Language("spa", "Spanish"),
            new Language("swa", "Swahili"),
            new Language("swe", "Swedish"),
            new Language("syr", "Syriac"),
            new Language("tgl", "Tagalog"),
            new Language("tam", "Tamil"),
            new Language("tel", "Telugu"),
            new Language("tha", "Thai"),
            new Language("tur", "Turkish"),
            new Language("ukr", "Ukrainian"),
            new Language("urd", "Urdu"),
            new Language("vie", "Vietnamese")
    };
}
