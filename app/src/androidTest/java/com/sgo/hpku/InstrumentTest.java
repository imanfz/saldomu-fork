package com.sgo.hpku;

import android.content.Context;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityTestCase;
import android.test.mock.MockContext;

import com.sgo.hpku.coreclass.PrefixOperatorValidator;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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

    String noHpTest = "089985229384";

    @Test
    public void PrefixOperatorTest()throws Exception{

        Context testContext = getInstrumentation().getTargetContext();
        PrefixOperatorValidator.OperatorModel operatorModel =
                PrefixOperatorValidator.validation(testContext,noHpTest);
        assertNotNull(operatorModel);
        assertThat("Telkomsel", is(equalToIgnoringCase(operatorModel.prefix_name)));
    }
}
