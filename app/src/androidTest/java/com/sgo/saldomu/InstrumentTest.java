package com.sgo.saldomu;

import android.content.Context;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.PrefixOperatorValidator;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

/**
 * Created by yuddistirakiki on 6/14/17.
 */

@RunWith(AndroidJUnit4.class)
@MediumTest
public class InstrumentTest {

    private String noHpTest = "089985229384";

    @Test
    public void PrefixOperatorTest()throws Exception{

        Context testContext = getInstrumentation().getTargetContext();
        PrefixOperatorValidator.OperatorModel operatorModel =
                PrefixOperatorValidator.validation(testContext,noHpTest);
        assertNotNull(operatorModel);
//        assertThat("Telkomsel", is(equalToIgnoringCase(operatorModel.prefix_name)));
    }

    @Test
    public void checkDateisMoreThan31DaysTest(){
        String curr_date = "2018-1-1";
        assertEquals("kurang dari 31 hari",false,DateTimeFormat.checkDateisMoreThan31Days(DateTimeFormat.convertStringtoCustomDate(curr_date)));

        curr_date = "2017-12-1";
        assertEquals("lebih dari 31 hari",true,DateTimeFormat.checkDateisMoreThan31Days(DateTimeFormat.convertStringtoCustomDate(curr_date)));

        curr_date = "2018-1-12";
        assertEquals("sama dengan hari ini",false,DateTimeFormat.checkDateisMoreThan31Days(DateTimeFormat.convertStringtoCustomDate(curr_date)));
    }
}
