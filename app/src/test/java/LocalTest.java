import android.content.Context;

import com.sgo.hpku.coreclass.NoHPFormat;
import com.sgo.hpku.coreclass.PrefixOperatorValidator;

import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

/**
 * Created by yuddistirakiki on 6/13/17.
 */

public class LocalTest {

    String noHpTest = "081285229384";



    @Test
    public void NoHPFormat08Test()throws Exception{
        assertEquals(noHpTest, NoHPFormat.formatTo08("081285229384"));
        assertEquals(noHpTest, NoHPFormat.formatTo08("6281285229384"));
        assertEquals(noHpTest, NoHPFormat.formatTo08("+6281285229384"));
        assertEquals(noHpTest, NoHPFormat.formatTo08("281285229384"));
        assertEquals(noHpTest, NoHPFormat.formatTo08("81285229384"));
    }






}
