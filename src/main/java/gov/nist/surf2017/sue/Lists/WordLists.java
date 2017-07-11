package gov.nist.surf2017.sue.Lists;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lns16 on 7/11/2017.
 */
public class WordLists {

    private static final List<String> EXTENDED_STOP_WORDS = Arrays.asList(
            "against", "although", "among", "amongst", "because", "cannot",
            "could", "dr", "during", "eg", "etc", "from", "his", "how", "itself", "multi", "others",
            "per", "pre", "should", "since", "something", "than", "them", "those", "toward",
            "unapproved", "unless", "until", "upon", "versa", "via", "we", "what", "when",
            "where", "whereas", "whether", "which", "whom", "whose", "without",
            "would", "you", "your", "yourself",

            // these are not in the WordNet database
            "proactively", "geocode", "geocoded", "geocoding", "subsample", "subsamples",
            "selectable", "unreceived", "backflushed", "intraoperation", "subassembly",
            "subassemblies", "reseller", "subline", "designator", "sublot",
            "nonconformant", "datetime", "timeperiod", "timestamp",
            "customization", "reenter", "rulebook", "asynchronously", "inline", "meta",
            "truckload", "postcondition", "transactional", "dept", "namespace", "middleware",

            "antillian", "aruban", "azerbaijanian", "belarussian", "congolais",
            "renminbi", "rican", "verde", "nakfa", "falkland", "kong", "kuna", "sheqel",
            "sri", "lanka", "malagasy", "denar", "rufiyaa", "oro", "nuevo", "st", "tolar",
            "somoni", "anga", "uruguayo", "fuerte", "vatu", "cefact", "uom", "xml"
    );
    private static final List<String> BOD_ACRONYMS = Arrays.asList(
            "BOD", "WIP", "IST", "MRP", "BOM", "RFQ", "ERP", "HRMS", "CSM", "PDM",
            "OAGIS", "CMMS", "BSR", "xsd", "TRACKINGID", "GL", "WIPSTATUS",
            "SEARCHTERM", "PRODUCTLINE", "Fedex", "USERAREA", "Schematron",
            "ISO", "Thh", "YYYY", "hh", "DDD",
            "URI", "UML", "MDA", "CFA", "SDR", "CFP",
            "RFID", "ISBN", "PLSS", "ABIE", "CRM", "JSON", "INCOTERMS",
            "UCC", "RUABIE", "TBG", "UPC", "JIT", "LTL", "FIIN", "DAAC", "CHK",
            "SHIPUNITSEQ", "SHIPUNITTOT", "NMFC", "MFAG", "ASN",
            "BO", // BODs becomes BO when normalized for camel case
            "OA"  // OAGi becomes OA when normalized for camel case
    );
    private static final HashMap<String, String> AUTOCORRECT;

    static {
        AUTOCORRECT = new HashMap<String, String>() {
            {
                put("acknowledgehronize", "Acknowledge or Synchronize");
                put("acknowledgehronized", "Acknowledged or Synchronized");
                put("acknowledgehronizing", "Acknowledging or Synchronizing");
                put("typiically", "typically");
                put("owne", "owner");
                put("resulst", "result");
                put("coresponding", "corresponding");
                put("proces", "Process");
                put("simpilify", "simplify");
                put("puchase", "Purchase");
                put("chagne", "Change");
                put("opportunityis", "Opportunity is");
                put("capabilit", "capability");
                put("salesorder", "Sales order");
                put("purchaseorder", "Purchase order");
                put("syn", "Sync");
                put("leadis", "Lead is");
                put("camcel", "Cancel");
                put("publishe", "publish");
                put("buiness", "business");
                put("respons", "response");
                put("updats", "update");
                put("facitliy", "facility");
                put("usethis", "use this");
                put("wher", "where"); //check
                put("fexibile", "Flexible");
                put("occured", "where"); //check
                put("tpically", "typically");
            }
        };
    }

    public static boolean isSpecialWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase()) || BOD_ACRONYMS.contains(term)) {
            return true;
        }
        return false;
    }

    public static String autocorrectTerm(String term) {
        if (AUTOCORRECT.containsKey(term.toLowerCase())) {
            return AUTOCORRECT.get(term.toLowerCase());
        } else {
            return null;
        }
    }
}
