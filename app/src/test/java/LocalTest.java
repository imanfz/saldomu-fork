import com.sgo.saldomu.coreclass.NoHPFormat;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
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
