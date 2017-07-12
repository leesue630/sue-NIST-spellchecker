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
            "subassemblies", "reseller", "subline", "designator", "sublot","incremented",
            "nonconformant", "datetime", "datetimes", "tele", "timeperiod", "timestamp","financials",
            "customization", "configurable", "configurator", "reenter", "rulebook", "asynchronously", "inline", "meta",
            "truckload", "postcondition", "transactional", "dept", "namespace", "middleware",
            "predefined","undeliverable","enabler","geospatial","evaluable","rec","forwarders","incl",
            "aromatics","ltd","intergovernmental",

            "antillian", "aruban", "azerbaijanian", "belarussian", "congolais",
            "renminbi", "rican", "verde", "nakfa", "falkland", "kong", "kuna", "sheqel",
            "sri", "lanka", "malagasy", "denar", "rufiyaa", "oro", "nuevo", "st", "tolar",
            "somoni", "anga", "uruguayo", "fuerte", "vatu", "cefact", "uom", "xml"
    );
    private static final List<String> BOD_ACRONYMS = Arrays.asList(
            // acronyms
            "ABIE","ABTA","AGF","ASC","ASN","AP","AQL",
            "BDEW","BLS","BOD","BOM","BRMA","BSR","BTS","BXA",
            "CEC","CEN","CFA","CFP","CFR","CHK","CIF","CIP","CMMS","CNC","CPSC","CPT","CRM","CSIO","CSM",
            "DAAC","DAT","DAP","DDP","DDTC","DHL","DISTRIBUTN","DNS","DSAA","DVGW",
            "EAN","ECPC","EDI","ERP","EXW",
            "FCA","FDIS","FIIN","FMCSA","FOB","FSIS","FTZ","FTZB",
            "GFTN","GIPSA","GL","GUID",
            "HRMS",
            "IANA","ICNCP","ICZN","IETF","IMDG","IOTC","ISA","ISBN","ISO","IST","ITC","ITIGG",
            "JAI","JASTPRO","JIPDEC","JIT","JSON",
            "LIMNET","LIMS","LTL",
            "MARAD","MDA","MFAG","MRP",
            "NABCA","NACCS","NHTSA","NMFC","NMFS",
            "OAGIS","ODETTE","ODTC","OES","OFAC","OFM","ORIGREF","OSJD",
            "PDM","PHMSA","PLSS","PN","PQALZM",
            "RAA","RFID","RFQ","RINET","RUABIE",
            "SCAC","SDR","SIFA","SSN",
            "TBG","TDB","TTB",
            "UCC","UML","UNCCS","UOM","UPU","URI","UPC","USACE","USAID","USCG","USTR",
            "VRML",
            "WIP",
            "xsd",

            // words
            "Geo","Fedex","INCOTERMS","PRODUCTLINE","Schematron","SEARCHTERM","TRACKINGID","USERAREA","WIPSTATUS",
            "userid", "usergroup","REQLINE","lang","Tchechian","Istilled","Dagang","Banque","Geldausgabeautomaten",
            "Gesellschaft","Associazione","Bancaria","Italiana","Socieata","Interbancaria","Automazione","Telekurs",
            "Interbank","Hong","Interpay","Girale","Ediel","Gesamtverband","Deutschen","Versicherungswirtschaft",
            "Graydon","Creditreform","Kreditversicherungs","Telcom","TELEBIB","Exis","Comite",
            "Ufficio","Systeme","Anwendungen","Produkte","Teikoku","Databank","Logistique","Urbaine",
            "Hashemite","Bronnoysund","EDIFACT",

            "Europeenne","Constructeurs","Centrale","l'importation","carburants","et","liquides",
            "Institut","Statistique","et","Economiques","eingetragener","Verein","Aktiengesellschaft",
            "responsabile","gestione","partite","und","Geonomenclature",

            "Thh","YYYY","hh","DDD",

            "rfc7946",

            "BO", // BODs becomes BO when normalized for camel case
            "OA"  // OAGi becomes OA when normalized for camel case
    );

    private static final HashMap<String, String> BOD_ACRONYM_DICTIONARY;

    static {
        BOD_ACRONYM_DICTIONARY = new HashMap<String, String>() {
            {
                put("ABIE", "Aggregate Business Information Entity"); //check
                put("ASN", "Advanced Shipment Notice");
                put("AP", "");
                put("BOD", "Business Object Document");
                put("BOM", "Bill of Materials");
                put("BSR", "Business Service Request");
                put("CFA", "Chartered Financial Analyst"); //check
                put("CFP", "");
                put("CHK", "Check");
                put("CMMS", "Computerized Maintenance Management System");
                put("CNC", "Computer Numerical Control");
                put("CRM", "Customer Relationship Management");
                put("CSM", "Component Supplier Management");
                put("DAAC", "");
                put("DHL", "Carrier like UPS");
                put("DISTRIBUTN", "");
                put("EDI", "");
                put("ERP", "Enterprise Resource Planning");
                put("FIIN", "");
                put("GL", "");
                put("HRMS", "Human Resource Management System");
                put("ISBN", "International Standard Book Number");
                put("ISO", "");
                put("IST", "");
                put("JIT", "");
                put("JSON", "");
                put("LIMS", "Laboratory Information Management System");
                put("LTL", "Less Than Truck Load");
                put("MDA", "");
                put("MFAG", "");
                put("MRP", "Material Resource Planning");
                put("NMFC", "");
                put("OAGIS", "Open Applications Group Integration Specification");
                put("ORIGREF", "");
                put("PDM", "Product Data Management");
                put("PLSS", "");
                put("PN", "");
                put("RFID", "Radio Frequency Identifier");
                put("RFQ", "Request for Quote");
                put("RUABIE", "");
                put("SDR", "");
                put("SSCC", "Serial Shipping Container Code");
                put("TBG", "");
                put("UCC", "Uniform Code Council");
                put("UML", "");
                put("UOM", "Unit of Measure");
                put("URI", "");
                put("UPC", "");
                put("VRML", "Virtual Reality Markup Language");
                put("WIP", "Work-in Progress");
                put("xsd", "");
            }
        };
    }

    private static final HashMap<String, String> AUTOCORRECT;

    static {
        AUTOCORRECT = new HashMap<String, String>() {
            {
                put("abiility", "ability");
                put("accuratly", "accurately");

                put("acknowledgehronize", "Acknowledge");
                put("acknowledgehronized", "Acknowledged");
                put("acknowledgehronizing", "Acknowledging");
                put("acknowledgehronization", "Acknowledgment");
                put("acknowlegdes", "acknowledges");
                put("acknowledgeed","Acknowledged");
                put("acknowledgeitional","Acknowledge");

                put("activityinformation","Activity information");
                put("agiven", "a given");
                put("allternate", "alternate");
                put("arbitary", "arbitrary");
                put("associacted", "associated");
                put("assocated", "associated");
                put("availble", "available");

                put("buiness", "business");
                put("buisness", "business");

                put("camcel", "Cancel");
                put("cancelhronize", "Cancel");
                put("cancelhronized","Canceled");
                put("cancelhronizing","Canceling");

                put("carrirer", "carrier");
                put("capabilit", "capability");
                put("catalogmay", "Catalog may");
                put("clategorize", "categorize");

                put("chagne", "Change");
                put("changehronize", "Change");
                put("changehronizing", "Changing");
                put("changehronized", "Changed");

                put("choosen", "chosen");
                put("codelist", "code list");
                put("commerical", "commercial");
                put("compoenent", "component");
                put("compenents", "components");
                put("consisit", "consist");
                put("constituient", "constituent");
                put("consumptn", "consumption");
                put("cor", "core ");
                put("corrdinator", "coordinator");
                put("coresponding", "corresponding");
                put("currentcy", "currency");
                put("daye", "date");
                put("daywithin", "day within");
                put("dpending", "depending");
                put("deliveried", "delivered");
                put("deorecate", "Deprecate");
                put("depricated", "Deprecated");
                put("destinated", "destined");

                put("doucment", "document");
                put("documet", "document");
                put("docment", "document");

                put("dutation", "duration");
                put("duratation", "Duration");
                put("eiher", "either");
                put("elments", "elements");
                put("enrty", "entry");
                put("esitmated", "estimated");
                put("facitliy", "facility");
                put("facor", "favor");
                put("fexibile", "Flexible");
                put("freeform", "free-form");
                put("fpr", "for");
                put("fullfil", "fulfill");
                put("fullname", "full name");
                put("identifyies", "Identifies");
                put("idenitifies", "Identifies");
                put("idenitfies", "Identifies");
                put("identifing", "identifying");
                put("indicat", "indicate");
                put("inidicates", "Indicates");

                put("informatoin", "information");
                put("informoration", "Information");
                put("infoi\\rmatoin", "information");

                put("iterms", "Items");
                put("leadis", "Lead is");
                put("lline", "Line");
                put("maor", "major");
                put("movemented", "moved");
                put("mulitple", "multiple");
                put("mulitplier", "Multiplier");

                put("notifyhronized", "notified");

                put("oe", "or");
                put("operatations", "Operations");
                put("opprotunity", "opportunity");
                put("opportunityis", "Opportunity is");
                put("occurre", "occurred");
                put("occurence", "occurrence");
                put("occurance", "occurrence");
                put("overriden", "overridden");
                put("owne", "owner");
                put("pary", "Party");
                put("parrent", "parent");
                put("perferred", "preferred");
                put("polyons", "polygons");
                put("pecision", "precision");
                put("predicessor", "Predecessor");
                put("preventitive", "preventative");

                put("proces", "Process");
                put("processhronize", "Process");
                put("processhronizing", "Processing");
                put("processhronized", "Processesd");

                put("prodiuction", "production");
                put("publishe", "publish");
                put("puchase", "Purchase");
                put("purchaseorder", "Purchase order");
                put("quatities", "quantities");
                put("quanity", "quantity");
                put("receipient", "recipient");
                put("recieved", "received");
                put("reoccurrence", "recurrence");
                put("requestor", "requester");
                put("responsiblity", "responsibility");

                put("respons", "response");
                put("responseed", "Responded");
                put("responseing", "Responding");
                put("responsehronize", "Respond");
                put("responsehronized", "Responded");
                put("responsehronization", "Response");
                put("responsehronizing", "responding");
                put("responsement", "response");
                put("responsements", "responses");

                put("resulst", "result");
                put("reveune", "revenue");
                put("salesorder", "Sales order");
                put("setial", "Serial");
                put("sequencial", "sequential");
                put("specifi", "specific");
                put("specifing", "specifying");
                put("sideof", "side of");
                put("simpilify", "simplify");
                put("subsituted", "substituted");
                put("superceding", "superseding");
                put("suppier", "Supplier");
                put("syn", "Acknowledge, CancelAcknowledge, Cancel, ChangeAcknowledge, Change, Process, Sync, or SyncResponse");
                put("teardown", "tear down");
                put("termperature", "temperature");
                put("transportaion", "transportation");
                put("tme", "time");

                put("typiically", "typically");
                put("tpically", "typically");

                put("uniques", "unique");
                put("uniquily", "uniquely");
                put("updats", "update");
                put("usethis", "use this");
                put("wher", "where");
                put("whocm", "whom");

                put("scc", "SSCC");
                put("shipunittot", "ShipUnitTotalID");
                put("shipunitseq","ShipUnitSequenceID");
                put("withissues", "with issues");

                // Proper Noun
                put("ethopian", "Ethiopian");
                put("comoro", "Comoros");
                put("morrocan", "Moroccan");
                put("carribean", "Caribbean");
                put("venezuala", "Venezuela");
                put("austrialian", "Australian");
            }
        };
    }

    public static boolean isSpecialWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase()) || BOD_ACRONYMS.contains(term)) {
            return true;
        }
        return false;
    }

    public static boolean isSpecialWordWOBODAcronyms(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase())) {
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
