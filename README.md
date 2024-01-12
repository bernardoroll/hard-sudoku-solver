# Hard Sudoku Solver


<a href="https://www.codewars.com/kata/5588bd9f28dbb06f43000085/train/java">This is a Kata from code wars site</a>
<p>
There are several difficulty of sudoku games, we can estimate the difficulty of a sudoku game based on how many
cells are given of the 81 cells of the game.
</p>
<ul>
<li>Easy sudoku generally have over 32 givens</li>
<li>Medium sudoku have around 30–32 givens</li>
<li>Hard sudoku have around 28–30 givens</li>
<li>Very Hard sudoku have less than 28 givens</li>
</ul>
<b>Note:</b> The minimum of givens required to create a unique (with no multiple solutions) sudoku game is 17.
<p>
A hard sudoku game means that at start no cell will have a single candidates and thus require guessing
and trial and error. A very hard will have several layers of multiple candidates for any empty cell.
</p>
<p>
<b>Task:</b>
</p>
<p>
Write a function that solves sudoku puzzles of any difficulty. The function will take a sudoku grid and it should
return a 9x9 array with the proper answer for the puzzle.
</p>
<p>
Or it should raise an error in cases of: invalid grid (not 9x9, cell with values not in the range 1~9);
multiple solutions for the same puzzle or the puzzle is unsolvable
</p>
<p>
<em>Java users: throw an IllegalArgumentException for unsolvable or invalid puzzles or when a puzzle has
multiple solutions.</em>
</p>
<b>NOTE:</b> We should try to use some more advanced techniques to solve any difficulty sudoku puzzle.
As mentioned by Chat-GPT these techniques can be:
<br>
<em>Apply various logical deductions such as "naked singles," "hidden singles," "naked pairs," "hidden pairs,"
"naked triples," and "hidden triples" to fill in more cells. These deductions involve analyzing the possible
values for cells in a row, column, or box to identify patterns and eliminate possibilities.</em>
