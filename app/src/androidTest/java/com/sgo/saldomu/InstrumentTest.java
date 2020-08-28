package com.sgo.saldomu;

import android.content.Context;
import androidx.test.filters.MediumTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.PrefixOperatorValidator;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
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
