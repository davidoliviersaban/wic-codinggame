import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestStupidAI.class, TestEngine.class, TestScore.class, TestHillClimbingAI.class })
public class TestAll {

}
