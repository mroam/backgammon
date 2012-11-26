import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ AIPartialMoveTest.class, AITest.class, BoardTest.class,
		DiceTest.class, GameTest.class, MathDemo.class, MoveTest.class,
		PartialMoveTest.class, PlayerTest.class, PointBuildScorerTest.class,
		PointBuildStrategyTest.class, RaceStrategyTest.class,
		StartGameTest.class })
public class AllTests {

}
