package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 1/16/2015.
 */

public class DenomModel {

    public static final String[][] allDenom = {  {"MS5", "5375", "T- SEL REG 5000"},
                                    {"MS10", "10275", "T- SEL REG 10000"},
                                    {"MS20", "20025", "T- SEL REG 20000"},
                                    {"MS25", "25425", "T- SEL REG 25000"},
                                    {"MS50", "49225", "T- SEL REG 50000"},
                                    {"MS100", "97225", "T- SEL REG 100000"},

                                    { "MR5", "5351", "XL REG 5000" },
                                    { "MR10", "10376", "XL REG 10000" },
                                    { "MR25", "25100", "XL REG 25000" },
                                    { "MR50", "49949", "XL REG 50000" },
                                    { "MR100", "99596", "XL REG 100000"},

                                    {"MN1", "1050", "AXIS REG 1000"},
                                    {"MN5", "5325", "AXIS REG 5000"},
                                    { "MN10", "10349", "AXIS REG 10000" },
                                    { "MN20", "20100", "AXIS REG 20000" },
                                    { "MN25", "25373", "AXIS REG 25000" },
                                    { "MN50", "51100", "AXIS REG 50000" },
                                    { "MN100", "101500", "AXIS REG 100000" },

                                    { "MI5", "5350", "INDOSAT REG 5000" },
                                    { "MI10", "10350", "INDOSAT REG 10000" },
                                    { "MI12", "12236", "INDOSAT REG 12000" },
                                    { "MI25", "25150", "INDOSAT REG 25000" },
                                    { "MI50", "49150", "INDOSAT REG 50000" },
                                    { "MI100", "97700", "INDOSAT REG 100000" },

                                    { "MIG5", "5901", "INDOSAT GPRS 5000" },
                                    { "MIG10", "10926", "INDOSAT GPRS 10000" },
                                    { "MIG25", "25223", "INDOSAT GPRS 25000" },

                                    { "MIS5", "5901", "INDOSAT SMS 5000" },
                                    { "MIS10", "10926", "INDOSAT SMS 10000" },
                                    { "MIS25", "25223", "INDOSAT SMS 25000" },

                                    { "MF5", "5200", "SMARTFREN REG 5000" },
                                    { "MF10", "10025", "SMARTFREN REG 10000" },
                                    { "MF20", "19874", "SMARTFREN REG 20000" },
                                    { "MF25", "24748", "SMARTFREN REG 25000" },
                                    { "MF30", "29773", "SMARTFREN REG 30000" },
                                    { "MF50", "49494", "SMARTFREN REG 50000" },
                                    { "MF75", "75030", "SMARTFREN REG 75000" },
                                    { "MF100", "98985", "SMARTFREN REG 100000" },
                                    { "MF150", "147986", "SMARTFREN REG 150000" },
                                    { "MF200", "197986", "SMARTFREN REG 200000" },
                                    { "MF225", "225030", "SMARTFREN REG 225000" },
                                    { "MF300", "295898", "SMARTFREN REG 300000" },
                                    { "MF500", "492877", "SMARTFREN REG 500000" },

                                    { "ME1", "1117", "ESIA REG 1000" },
                                    { "ME5", "5225", "ESIA REG 5000" },
                                    { "ME10", "10225", "ESIA REG 10000" },
                                    { "ME11", "11005", "ESIA REG 11000" },
                                    { "ME15", "15030", "ESIA REG 15000" },
                                    { "ME20", "20030", "ESIA REG 20000" },
                                    { "ME25", "24623", "ESIA REG 25000" },
                                    { "ME50", "48250", "ESIA REG 50000" },
                                    { "ME100", "95900", "ESIA REG 100000" },

                                    { "MT5", "4812", "FLEXI REG 5000" },
                                    { "MT10", "9607", "FLEXI REG 10000" },
                                    { "MT20", "19196", "FLEXI REG 20000" },
                                    { "MT25", "24183", "FLEXI REG 25000" },
                                    { "MT50" , "47989", "FLEXI REG 50000" },
                                    { "MT100", "96077", "FLEXI REG 100000" },
                                    { "MT150", "141705", "FLEXI REG 150000" },
                                    { "MT250", "236175", "FLEXI REG 250000" },

                                    { "MH1", "1050", "THREE REG 1000" },
                                    { "MH5", "4999", "THREE REG 5000" },
                                    { "MH10", "9999", "THREE REG 10000" },
                                    { "MH20", "19899", "THREE REG 20000" },
                                    { "MH30", "29848", "THREE REG 30000" },
                                    { "MH50", "49746", "THREE REG 50000" },
                                    { "MH100", "99493", "THREE REG 100000" },

                                    { "MG5M", "33416", "THREE INTERNET 500 MB" },
                                    { "MG1G", "47687", "THREE INTERNET 1 GB" },
                                    { "MG2G", "71556", "THREE INTERNET 2 GB" },
                                    { "MG5G", "119093", "THREE INTERNET 5 GB" }
                                 };

    private String id    = "";
    private String price = "";
    private String name  = "";



    public DenomModel(String _id, String _price, String _name)
    {
      setId(_id);
      setPrice(_price);
      setName(_name);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
