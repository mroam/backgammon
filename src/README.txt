------------------------------------------------------------------------
This is the JBackgammon README file!
------------------------------------------------------------------------

PROJECT TITLE: Backgammon with AI

PURPOSE OF PROJECT: build backgammon with artificial intelligence.

VERSION or DATE: 2012 Jun (Left off in PointBuildScorer.howImportantIsThisPoint( )

KILLS: has defeated a middle schooler in May 2012!

HOW TO START THIS PROJECT: 
"Game" is the main class, and can be brought to life several ways...
In BlueJ instantiate "JBackgammon(false)" (boolean says whether networked or not)
and tell it to init( ).
Alternative: use Safari or Firefox to browse JBackgammonApplet.html (Chrome doesn't work)
or in terminal say "java JBackgammon" in this directory. 
Handy compile option:   javac -Xlint:unchecked JBackgammon.java
There is a jar that works on mac if double-clicked.
(The html page gets security exceptions re bluej core .jars.)


AUTHORS: 
Original code from JBackgammon (http://jbackgammon.sf.net) [website nonexistent in 2012], 
Copyright (C) 2002
by George Vulov <georgevulov@hotmail.com> & Cody Planteen <frostgiant@msn.com>.
[The TerraIM (instant messaging) project (http://terraim.sourceforge.net/index.php)
has updates in 2008 from georgevulov@hotmail.com]
[A search online in fall 2012 finds a Cody Planteen involved with a
"Quiklst" project that is part of a Georgia Tech embedded controller.]

Revised by Josh G. & Julien S. & Mike Roam starting Sep 2011.


USER INSTRUCTIONS:
Roll the dice, then click a column that you want to move from. The possible legal moves
will get little buttons beneath them and you can press them to choose which your move.


Backgammon Rules:
You have to clear all your blots off the bar before you may do anything else, so 
is there a way to have both black and white blots on the bar? Yes, if coming back 
onto the board from the bar lands on the other color.
If you roll doubles you get 4 moves. Can't forfeit turns if a move is available!


...How This Code works
White is trying to end on points 19-24 and Black is ending on points 1-6.
The "Board" class keeps track of what color is on a particular "point" ("column", "spike") and 
how many blots are on that point.
If you specify a number of number of blots on a point, they are black by default if you don't say.
Still not sure if point 25 is being used for anything. Program dies if it doesn't create 25 points. 
(There are separate white_bar, white_bear, black_bar, black_bear variables.)


...Bugs
[ ]Bearing off doesn't always call ShowPartialMoveLabel.drawOnPoint, or at least not properly.


...High priority
[ ]Label the board with Black's numbering scheme, also. Just has white's point numbers showing.
[ ]UNDO (would have to remember pre-move board, OR all side-effects such as a move that knocks
opponent onto the bar).
[ ]AI's bearOff moves are crazy when Game.showPartialMoves and white's bearOffs don't show at all.
[ ]See how long it takes to create and score and rank 100 (non-default) Boards!
[ ]The value of a board should include pip count factored in somehow! Oh, it does implicitly 
since there will be fewer protection points with a blot bumped out to the bar.
[ ]Get AI to do a better job of choosing a move. This will involve finding combinations of partial 
moves, perhaps trying using one roll first and then the other roll first, especially if trying to
come in from the bar.


...Things to think about and decide
[ ]Should a MoveAndScore also hold a link to the Scorer that was used? (and to the board?)
[ ]How many moves to use the startGameStrategy for before switching to pointBuildStrategy.
[ ]How far to look ahead
[ ]ability to move stacks of blots, probably only when you have doubles. (Drag'em on ipad?)
[ ]should winner of starting roll get to re-roll for their first move?
[ ]Should PointBuildStrategy not only add up happiness about his own blots but unhappiness about 
where the opponent's blots are (which is NOT the same thing as opponent's happiness about where 
his blots are, which might also be included, and even include opponent's unhappiness of where our 
blots are?)
[ ]Rather than merely summing up the values of the blots that are on points, also sum up how
much risk there is of exposed blots (of either color) ACTUALLY getting hit.
[ ]is this a bug? Do I have to use smaller dice before using over-big dice to bear off 
higher points? Example: white is bearing off (to 25+), has blots on 24,23,22. Rolls 1,6.
I click on blot on 24 and my only choice is to move it to 23, no chance to use the 6 to 
bear off. Do I HAVE to use small rolls first?
[ ]When computing the value of points on the board, how much more is a point with 3 blots on it 
compared to a point with 2 blots on it??
[ ]"Backgammon.java uses unchecked or unsafe operations. recompile with   -Xlint:unchecked for details."
[ ]Suppose there are 2 exposed white blots (b1 and b2) that are respectively s1 and s2 steps from
the start. (Distance to bearing off and escaping is 25-s.) The blots have individual probability 
(d1 and d2) of being hit by a black blot. How do we decide which to move? 
We could give them scores for comparing...the one that has gone farther has more to lose (higher s).
So maybe score = (s * d)?   or s + d? 
[ ]Does the random number generator have a seed and follow the same series of rolls every time??
[ ]Should AI be an interface?


...Future Plans:
[ ]Command objects should have toString and fromString for unambiguous text representations that can 
be used in those String packets for network play.
[ ]PointNum class should exist, basically wrap an int, and sometimes also wrap a PlayerColor–perhaps 
have a SpecialPointNum class that also requires PlayerColor for bar and bear–, and have constructors 
and distance calculators that return NO_SUCH_MOVE or ILLEGAL_MOVE for special places unless you also 
specify a PlayerColor
[ ]"Tournament" has "Games" that have "Boards" and "Players" (that might be AI or human (network?))
[ ]When calculating odds that enemy can hit you, must realize that enemy with a blot needing to re-enter 
from bar is constrained in the moves that can hit you since the enemy has to get onto the board with 
exact roll before being able to do anything else.
[ ]Attribute backgammon. At method granularity anything we keep--the legit? & test & exception methods 
are all mine. as is AI, Move, PartialMove, howMany...(), 
[ ]Ted's suggestion: sometimes let a blot be hit so it can go back and mess around in
the opponent's final quadrants.
[ ]Alter gameStartStrategy to work for white as well as black so we can press AI move!
(Merely requires pointBuilder to call some utility that switches point locations from
black p.o.v. to white p.o.v.)
[ ]Use the "@Test(expected=IllegalArgumentException.class)" heading before a test to 
tell it to expect certain exceptions.
[ ]Our AI watches the opponent's style and quality of play and learns to anticipate.
[ ]Forfeit Move should be unavailable unless you're stuck. It's the rules! (Perhaps leave it
on when in testing mode.)
[ ]Let WolframAlpha calculate the odds for us!
[ ]Worry about the odds of a single actually being hit (depending on opponent's position)
[ ]ComputerMove button should be disabled during white's turn!
[ ]To show used doubles, could put 2 extra numbers beside them e.g. "5 5" that can be crossed off.
[ ]Wish dice were clickable for rolling instead of (or also) pressing "roll dice" button.
[ ]Was thinking about keeping dice sorted: just be sure not to sort 'em until deciding which
player starts the game!
[ ]AI should take last instead of first legal move when being random!
[ ]"moveHere" buttons could be larger and (if translucent) overlapping the points themselves 
(and the starterLoc should be highlighted as well, with a click upon it meaning cancel Choice). 
[ ]AI should report its move, at the least. I now have recentMoveEnd[4] = new JButton() and 
recentMoveStart[4] for moving to endpoints of recent partialMoves. If I were keeping a list
of recent commands, I could tell mainBoard's paint to use the endpoints of the latest partials.
[ ]Have one button that merely shows AI's recommendation without doing it
[ ]AI Move button should call Game.paint after rolling dice. I'm still
thinking Board should have a doRoll that includes all of Game.doRoll's 
startGame and turnOver smarts.
[ ]Game doesn't acknowledge "gammon" (which is what exactly? all blots bear off before
opponent gets any off, perhaps?)
[ ]Game.forfeitTurn says "usable dice are" should say "usable dice roll values is/are"
[ ]Hey: rollDice on gui is smart enough to forfeit turn, but ai's call to rollDice isn't. What's the diff?
[ ]Optimize the move chooser in AI by only keeping best hypothetical board so far instead of all of 'em.
Bayard suggestion.
[ ]Boards need a "equals( )" method for comparing whether different Moves get same blot placement.
[ ]On GUI, Mark the dice that have been used, or at least say how many moves avail. X out the dice or fade?
[ ]StartGameStrategy should provide alternate moves, or at least notice when the order of
partials doesn't matter in case one path is possible while another equivalent is not: 
e.g. use 6roll to go from 24to18 and then use4 go 18to14
should have an alternate in case 18 is blocked: use 4roll go 24to20, then use6roll go 20to14 
[ ]AI's new moves could be shown by coloring where they came from? and ended? (next paint will repair)
[ ]potDest1 & potDest2 in board should be array or ArrayList! (And in board controller? Only if doMove
and doPartialMove don't use them anymore.)
[ ]If dice were always sorted, then I'd always know that die1 had lower or equal value to die2.
[ ]There should be a kind of Move (and PartialMove) that means "forfeitTurn" !!
[ ]Rephrase that "(full)Move" are all called "Turn" or "MultiMove", while "partialMove" can then be 
called "Move"!
[ ]Boards have names ("main" or "temp01") for debug printing.
[ ]Game keeps history of the official moves (not the hypothetical ones). [ ]Have undo!
[ ]Player's nextPoint( ) and prevPoint( ) utility functions might be better off in a Point class 
[ ]Used dice fade out or get crossed out or something.
[ ]in Dice the highest/lowest val ( & which ) ought to use the same code. 
[ ]for iPad interface: drag pieces with zoom-in for aiming at the target.
[ ]Have some buttons on the screen to choose whether AI is white or black.
[ ]supermegahappyscore( ) cares that blots are on the bar, should care even more depending on where
the blot got sent back from. (Did he lose a valuable point?)
[ ]More than 2 players, square board (Julien idea).
[ ]Bayard suggests physics so dice and blots can bounce and move and click.
[ ]Computer should be able to play itself (to test moves and strategies, for example). To make
this happen we have to let the computer press the "next move" button for itself.
[ ]Need more "scoring functions" (danger, 
[ ]At start up ask whether network game or two humans or human vs ai.
[ ]Learn how a save bunch of moves so that we can compare them and choose the best.
[ ]Build "points" (by protecting them) (and [ ]keep track of the risk)
[ ]GUI offers choice of which starting board. For now Board is hard-wired.
[ ](Maybe the silly buttons should just have their own names and listeners like a grown up program!)
[ ]Add a button to call Board's "makeAlmostDoneGame" method for testing end game.
[ ]Add a third die for fun.
[ ]Show (on the gui) how many blots are on the board. There is already something showing how many
blots have beared off, and we could extend it.
Perhaps this could also show how many steps left before finishing.
[ ]Perhaps for convenience be able to jump the value of combined dice, if the intermediate step is legal.



...History
2012 Nov 26:
trying to update on github and include javadoc as website on github.

2012 May 29:
[x]Added a HitBlot class and now board keeps track of all HitBlot during each turn, for Scorers
to use. Partial example in PointBuildScorer.

2012 May 16:
[x]AI uses the relevant strategy for remaining partialMoves after coming in from bar. (used to be random)

2012 May 13:
[x]PointBuildStrategy is using the recursive Board.bestLegalMove! (It skipped a chance
to knock an opponent onto bar.)
[ ]Improved ai's switchStrategy, which used to choose PointBuildStrategy only when startGame was stuck.

2012 May 11:
[x]Added a "MoveWithScore" Class and Scorer class for recursing in Board.bestLegalMove, 
returning best Move AND its score.
MoveWithScore has boolean hasBeenScored and a Scorer.
Moves could instead conceivably hold a score field stating "the value of this move
when applied to startingBoard and measured with scoring function S"... ?? but complicates Move
class.
[x]Test the new Board.bestLegalMoves( ) recursive draft.

2012 May 5:
[x]Board has toString, and Board.allLegalMoves( ) has new recursive sketch.

2012 May 4:
[x]AI seems to be using its smart moves in startGame (neatly catalogued in startGameStrategy header).
[x]Consolidated the doMove methods from Move and Game into board.
[x]Solved insidious bug: clicking a non-moveable blot was leaving the blot selected
so you couldn't instead click a legal blot. (And I don't think the cancelChoice button
was available, but now I don't remember.)

2012 May 3:
[x]Used dice are marked. (But not for doubles until you use 3 of them.)
[x]Label numerically all the points. (Is optional.)
[x]drawDice includes checking whether used, and draws X over die accordingly.

2012 May 2:
[x]Boards can be compared for equality (of blot positions including bar & bear. Doesn't care
whether a point is selected).
[x]Winning causes the game to end.
[x]Passes all tests by fixing issue of which methods actually marks die as used among 
doPartialMove( ), moveBlot( ), bearOff( ), and comeInFromBar( ). Not doPartialMove, but the
other 3, which it calls.
[x]EndPartialMove checks for canMove( ), do forfeitTurn if necessary.
[x]Uh-oh, AI is now forfeiting turn when bringing black in from bar is blocked.
[x]Can't roll dice again until turn is done!
[x]If white is stuck on bar (rolls can't bring it in), now there's "forfeit turn" button to
forfeit rest of turn
[x]StartGameStrategy's Chatty test is picking the good moves.
[x]StartGameStrategy now checks that it is actually choosing possible moves, and all 
strategies have ability to try to get in from bar.
[x]AI returns a suggested "Move" and somebody else handles moving. -gy suggestion 2011

2012 Apr 30:
[x]Bar has to be clickable, somehow.

2012 Apr 27: 
[x]black (AI) could come in from bar by hand, but failed to come in under AI direction!!
(handleBar was expecting selected blot (on selectedPointLoc) to have be clicked on gui.)

2012 Apr 24: fixed countless canMove and (manual) goToBar and bearOff errors!
[x]Using     if (this == myGame.mainBoard ) {
to distinguish hypothetical boards from myGame's real public board.
[x]the "can bear off using inexact" now works.

2012 Apr 19: Big job replacing playerColor with "Player" class instead of int.
2012 Apr 3: Dice are passing all their tests, including lowestUnused, highestUnused, etc.

2012 Mar 25: doubletMoveCountdown wasn't getting adjusted when dice were
being given explicit values, so countdown was 2 for doubles. Is fixed!

2012 Feb 9: Board UI shows whose move it is! Human can't come in from bar,
and AI has to be brought in from bar manually. AI still dies
when a blot is near the end but can't bear off yet.

2012 Feb 1: compiles, computer makes its own partial moves (user has to press 
"Computer Move" button twice (or 4 times for doubles), dumb AI is still taking 
first possible move. If the first possible move is attempt to bear off (and other
blots aren't yet in final quadrant) the AI gets stuck.

Changes along the way, don't know when:
[x]Remember to check whether the caller of Board.bearOff( ) uses up another dice roll?? (I hope so).
[x]Make legal random moves. Note: the JBackgammon.java file has a "canLandOn( )" method that we should
perhaps study.
[x]Add English comments to more of the code.
[x]Realize that dice rolls get totally used up except when you're bearing off.
[x]Make the computer do (legal) moves.
[x]Do formal start procedure: Each player rolls one dice (one white, one black): high number goes
first using those two rolls. (For tie, roll again.)
[x]Why is "allLegalPartials" printing ALL of them twice? Is its arraylist double booked? 
They're in the same order, but could it be pt1moves5+pt1moves2, pt1moves2+pt1moves5 ??
Nevermind, another method was printing.
[x]White was able to use die1 twice, even though not doubles, so I suspect "usedDice" isn't working right. 
[x]White knocked black onto bar and the move didn't count, nor did its (how many?) successors, so white 
got subsequent infinite (or 4?) free moves!
Fixed: obob in setUsed( )
[x]FButtons array should use static int's when talking about buttons instead of hardcoded numbers.
[x]Make games that begin almost completed so we can test bearing off. 
     Note: the "Board" constructor method could do this.
[x]Changed "drawMen" to "drawBlots".
(For horrible example of hardcoded undocumented numbers see in JBackgammon's "HandlePoint( )" method.)
[x]GUI says whose turn it is.
[x]"New Game" button doesn't enable the "Roll Dice" button so white can't make her first move.
[x]Got AI to actually make its move. Problem was that "doPartialMove( )" wanted to know whether
to use the first or second dice, so our partialMoves are now remembering which dice is which.

2012 Jan 4: compiles, and AI is working but dumb: can't handle coming in from bar
and chooses first possible move. Human has to actually make the moves for the AI
(which is playing black).
started 2011 Oct 18

2011 nov 8
[x]PipCount now counts blots on the bar (that have to come back in: count as 25).

2011 oct 25. 
Added pip count method to board, and displays the result on GUI.
Added "Computer Move" button to JBackgammon (see setupGUI) but it doesn't do anything yet.
Added "AI" class which might have the brains inside. (Has to know about board so it can strategize.)

2011 oct 18
Changed most all methods to speak of "points" (instead of columns or spikes) and "blots" (pieces).
Set up a method in JBackgammon to start the board with other than default setup.
[x]Added functions to check legit point numbers and colors.



Notes:
as of Feb2012: AI calls thinkAndPlay which asks board for allLegalPartialMoves and does the "best."

JavaDoc Notes:
2012 nov 26: hmmm, with old bluej 2.5 the menu "tools:project documentation" got error "package org.junit does not exist"... fixed by getting bluej 3.0.8
Java 1.5 (aka java 5) api is at: http://docs.oracle.com/javase/1.5.0/docs/api/
Java 6 api is at:  http://docs.oracle.com/javase/6/docs/api/
Our local server api for java 6 is at: http://192.168.0.4/demos_and_samples/programming_references/java/docs/api6/docs/api/index.html
GitHub serves static HTML files that are in the gh-pages folder of your repo as http://your_username.github.com/your_repo/.
So after running "Tools:Project Documentation" in blueJ, I'm copying the automatically generated javadocs from doc folder to gh-pages folder. Then our uri should be "http://mroam.github.com/backgammon/"
