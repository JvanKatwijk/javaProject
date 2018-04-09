package utils;

public class Textmappers {

	public Textmappers () {}

	public	String getProgramType (short type) {
	   if (type > 0x40) 
	      return TABLE_12 [0];
	   if (type < 0)
	      return "       ";

           return TABLE_12 [type];
	}

	public	String getProgramLanguage (short language) {
	   if (language > 43) 
	      return TABLE_9 [0];
	   if (language < 0)
	      return "      ";

	   return TABLE_9 [language];
	}

	private final static String TABLE_9 [] = {
	   "unknown language",
	   "Albanian",
	   "Breton",
	   "Catalan",
	   "Croatian",
	   "Welsh",
	   "Czech",
	   "Danish",
	   "German",
	   "English",
	   "Spanish",
	   "Esperanto",
	   "Estonian",
	   "Basque",
	   "Faroese",
	   "French",
	   "Frisian",
	   "Irish",
	   "Gaelic",
	   "Galician",
	   "Icelandic",
	   "Italian",
	   "Lappish",
	   "Latin",
	   "Latvian",
	   "Luxembourgian",
	   "Lithuanian",
	   "Hungarian",
	   "Maltese",
	   "Dutch",
	   "Norwegian",
	   "Occitan",
	   "Polish",
	   "Portuguese",
	   "Romanian",
	   "Romansh",
	   "Serbian",
	   "Slovak",
	   "Slovene",
	   "Finnish",
	   "Swedish",
	   "Turkish",
	   "Flemish",
	   "Walloon"
	};

	private final static String TABLE_12 [] = {
	   "none",
	   "News",
	   "Current Affairs",
	   "Information",
	   "Sport",
	   "Education",
	   "Drama",
	   "Arts",
	   "Science",
	   "Talk",
	   "Pop Music",
	   "Rock Music",
	   "Easy Listening",
	   "Light Classical",
	   "Classical Music",
	   "Other Music",
	   "Weather",
	   "Finance",
	   "Children\'s",
	   "Factual",
	   "Religion",
	   "Phone In",
	   "Travel",
	   "Leisure",
	   "Jazz and Blues",
	   "Country Music",
	   "National Music",
	   "Oldies Music",
	   "Folk Music",
	   "entry 29 not used",
	   "entry 30 not used",
	   "entry 31 not used"
	};
}


