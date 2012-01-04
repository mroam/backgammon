NOTE: repo "backg" is better!! (2012 Jan 04, mjr)
------------------------------------------------------------------------
This is the JBackgammon README file.
------------------------------------------------------------------------

PROJECT TITLE: Backgammon AI and strategy

PURPOSE OF PROJECT: build backgammon a.i.

VERSION or DATE: started 2011 Oct 18

HOW TO START THIS PROJECT: 
 In BlueJ you can instantiate "JBackgammon(false)" (boolean says whether networked or not)
 or use Safari or Firefox to browse JBackgammonApplet.html (Chrome doesn't work)
 or in terminal say "java JBackgammon" in this directory. 
 (handy compile option:   javac -Xlint:unchecked JBackgammon.java

AUTHORS: 
Original code from JBackgammon (http://jbackgammon.sf.net), Copyright (C) 2002
by George Vulov <georgevulov@hotmail.com> & Cody Planteen <frostgiant@msn.com>.
Revised by Josh G. & Julien S. & Mike Roam starting Sep 2011.


USER INSTRUCTIONS:
Roll the dice, then click a column that you want to move from. The possible legal moves
will get little buttons beneath them and you can press them to choose which your move.


Backgammon Rules:
You have to clear all your pieces off the bar before you may do anything else, so 
is there a way to have both black and white pieces on the bar? Yes, if coming back 
onto the board from the bar lands on the other color.
Rolling Doubles gets 4 moves.


...How This Code works
White is trying to end on points 19-24 and Black is ending on points 1-6.
The "Board" class keeps track of what color is on a particular "point" ("column", "spike") and 
how many blots are on that point.
If you specify a number of number of blots on a point, they are black by default if you don't say.
Still not sure if point 25 is being used for anything. Program dies if it doesn't create 25 points. 
(There are separate white_bar, white_bear, black_bar, black_bear variables.)


...Bugs
[ ]ComputerMove doesn't do anything: is the listener wired up?


...Things to think about and decide
[ ]"Backgammon.java uses unchecked or unsafe operations. recompile with   -Xlint:unchecked for details.
[ ]Suppose there are 2 exposed white blots (b1 and b2) that are respectively s1 and s2 steps from
the start. (Distance to bearing off and escaping is 25-s.) The blots have individual probability 
(d1 and d2) of being hit by a black piece. How do we decide which to move? 
We could give them scores for comparing...the one that has gone farther has more to lose (higher s).
So maybe score = (s * d)?   or s + d? 
[ ]Does the random number generator have a seed and follow the same series of rolls every time??
[ ]Should AI be an interface?
[ ]Should 
[ ]Should AI.thinkAndPlay( ) return a "Move" and somebody else handles moving?? -gy suggestion 2011


...Future Plans:
[ ]GUI says whose turn, Xs out the dice that have been used, has undo, 
[ ]Computer should be able to play itself (to test moves and strategies, for example). To make
this happen we have to let the computer press the "next move" button for itself.
[ ]Need more "scoring functions" (danger, 
[ ]At start up ask whether network game or two humans or human vs ai.
[ ]Learn how a save bunch of moves so that we can compare them and choose the best.
[ ]Realize that dice rolls get totally used up except when you're bearing off.
[ ]Make the computer do (legal) moves.
[ ]Build "points" (by protecting them) (and [ ]keep track of the risk)
[ ]GUI offers choice of which starting board. For now Board is hard-wired.
[ ]Change "drawMen" to "drawBlots".
[ ]FButtons array should use static int's when talking about buttons instead of hardcoded numbers.
(For horrible example of hardcoded undocumented numbers see in JBackgammon's "HandlePoint( )" method.)
(Maybe the silly buttons should just have their own names and listeners like a grown up program!)
[ ]Add a button to call Board's "makeAlmostDoneGame" method for testing end game.
[ ]Add a third die for fun.
[ ]Make legal random moves. Note: the JBackgammon.java file has a "checkFair( )" method that we should
perhaps study.
[ ]Show (on the gui) how many blots are on the board. There is already something showing how many
blots have beared off, and we could extend it.
Perhaps this could also show how many steps left before finishing.
[ ]Study the move legality checker.
[ ]Make games that begin almost completed so we can test bearing off. 
     Note: the "Board" constructor method could do this.
[ ]Add English comments to more of the code.
[ ]Perhaps for convenience be able to jump the value of combined dice, if the intermediate step is legal.


...History
2011 nov 8
[x]PipCount now counts pieces on the bar (that have to come back in: count as 25).

2011 oct 25. 
Added pip count method to board, and displays the result on GUI.
Added "Computer Move" button to JBackgammon (see setupGUI) but it doesn't do anything yet.
Added "AI" class which might have the brains inside. (Has to know about board so it can strategize.)

2011 oct 18
Changed most all methods to speak of "points" (instead of columns or spikes) and "blots" (pieces).
Set up a method in JBackgammon to start the board with other than default setup.
[x]Added functions to check legit point numbers and colors. 
