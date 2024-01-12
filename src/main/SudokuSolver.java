package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <a href="https://www.codewars.com/kata/5588bd9f28dbb06f43000085/train/java">This is a Kata from code wars site</a>
 *
 * <p>
 * There are several difficulty of sudoku games, we can estimate the difficulty of a sudoku game based on how many
 * cells are given of the 81 cells of the game.
 * </p>
 * <ul>
 * <li>Easy sudoku generally have over 32 givens</li>
 *
 * <li>Medium sudoku have around 30–32 givens</li>
 *
 * <li>Hard sudoku have around 28–30 givens</li>
 *
 * <li>Very Hard sudoku have less than 28 givens</li>
 *
 * </ul>
 * <b>Note:</b> The minimum of givens required to create a unique (with no multiple solutions) sudoku game is 17.
 * <p>
 * A hard sudoku game means that at start no cell will have a single candidates and thus require guessing
 * and trial and error. A very hard will have several layers of multiple candidates for any empty cell.
 * </p>
 * <p>
 * <b>Task:</b>
 * </p>
 * <p>
 * Write a function that solves sudoku puzzles of any difficulty. The function will take a sudoku grid and it should
 * return a 9x9 array with the proper answer for the puzzle.
 * </p>
 * <p>
 * Or it should raise an error in cases of: invalid grid (not 9x9, cell with values not in the range 1~9);
 * multiple solutions for the same puzzle or the puzzle is unsolvable
 * </p>
 * <p>
 * Java users: throw an IllegalArgumentException for unsolvable or invalid puzzles or when a puzzle has
 * multiple solutions.
 * </p>
 *
 * <b>NOTE:</b> We should try to use some more advanced techniques to solve any difficulty sudoku puzzle.
 * As mentioned by Chat-GPT these techniques can be:
 * <br>
 * <i>Apply various logical deductions such as "naked singles," "hidden singles," "naked pairs," "hidden pairs,"
 * "naked triples," and "hidden triples" to fill in more cells. These deductions involve analyzing the possible
 * values for cells in a row, column, or box to identify patterns and eliminate possibilities.</i>
 */
public class SudokuSolver {

    private final int[][] puzzle;

    private int zerosInPuzzle = 0;

    private final int[][][] puzzlePossibilities;

    private enum DifficultyZeros {
        VERY_HARD(44),
        HARD(45),
        MEDIUM(47),
        EASY(49);

        private final int value;

        DifficultyZeros(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }

    }

    private final int[] allPossibilities = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public SudokuSolver(int[][] grid) {
        try {
            this.puzzle = grid;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid grid puzzle. The grid must be an int[][] type.\nMessage: " +
                    exception.getMessage());
        }
        puzzlePossibilities = new int[grid.length][grid.length][grid.length];
    }

    private void countZerosInPuzzle() {
        for (int[] row : puzzle) {
            for (int element : row) {
                if (element == 0) {
                    zerosInPuzzle += 1;
                }
            }
        }
    }

    /**
     * This method checks when the puzzle board has more than 9 rows or if any of its rows has
     * more than 9 elements on it and, at last it verifies if there is any invalid element on puzzle (e.g. an element
     * that is not a digit between 1 and 9).
     *
     * @throws IllegalArgumentException when any aforementioned conditions are found
     */
    private void startValidatingPuzzle() {
        if (puzzle.length != 9) throw new IllegalArgumentException("It's not a 9x9 grid.");
        if (zerosInPuzzle > 64) throw new IllegalArgumentException("Invalid Puzzle. The minimum of givens required " +
                    "to create a unique (with no multiple solutions) sudoku game is 17.");
        for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
            if (puzzle[rowIndex].length != 9) throw new IllegalArgumentException("The row at " + rowIndex +
                    " index has an invalid length.");
            for (int element : puzzle[rowIndex]) {
                if (element != 0 && (element < 1 || element > 9)) throw new IllegalArgumentException("At least one " +
                        "element is not a valid digit. Only digits from 1 to 9 are allowed.");
            }
        }
    }

    public int[] getCurrentRow(int rowIndex) {
        return puzzle[rowIndex];
    }

    public int[] getCurrentColumn(int columnIndex) {
        return Arrays.stream(puzzle).mapToInt((row) -> row[columnIndex]).toArray();
    }

    public int getRowQuadrant(int rowIndex) {
        return (int) Math.floor(rowIndex / Math.sqrt(puzzle.length));
    }

    public int getColumnQuadrant(int rowIndex, int columnIndex) {
        return (int) Math.floor(columnIndex / Math.sqrt(puzzle[rowIndex].length));
    }

    public int getCurrentQuadrantFirstRowIndex(int rowQuadrant) {
        return (int) (rowQuadrant * Math.sqrt(puzzle.length));
    }

    public int getCurrentQuadrantFirstColumnIndex(int columnQuadrant, int rowIndex) {
        return (int) (columnQuadrant * Math.sqrt(puzzle[rowIndex].length));
    }


    public List<Integer> getCurrentQuadrantNumbers(int currentQuadrantFirstRowPosition,
                                                   int currentQuadrantFirstColumnPosition) {
        List<Integer> quadrantNumbers = new ArrayList<>();
        for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
            for (int j = 0; j < Math.sqrt(puzzle[currentQuadrantFirstRowPosition + i].length); j += 1) {
                quadrantNumbers.add(
                        puzzle[currentQuadrantFirstRowPosition + i][currentQuadrantFirstColumnPosition + j]
                );
            }
        }
        return quadrantNumbers;
    }

    private int[] integerListToIntArray(List<Integer> list) {
        try {
            int[] quadrantToArray = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                quadrantToArray[i] = list.get(i);
            }
            return quadrantToArray;
        } catch (Exception e) {
            throw new RuntimeException("Impossible to convert list to array.");
        }
    }

    public int[] getCurrentQuadrant(int rowIndex, int columnIndex) {
        int rowQuadrant = getRowQuadrant(rowIndex);
        int columnQuadrant = getColumnQuadrant(rowIndex, columnIndex);
        int currentQuadrantFirstRowPosition = getCurrentQuadrantFirstRowIndex(rowQuadrant);
        int currentQuadrantFirstColumnPosition = getCurrentQuadrantFirstColumnIndex(columnQuadrant, rowIndex);

        List<Integer> quadrantNumbers = getCurrentQuadrantNumbers(currentQuadrantFirstRowPosition,
                currentQuadrantFirstColumnPosition);

        return integerListToIntArray(quadrantNumbers);
    }

    /**
     * This method calculates the symmetric difference between two arrays, more specifically the
     * array that contains all possible numbers (usually 1 to 9 digits) and the current set of possible
     * numbers that already exists on row, column or quadrant.<br><br>
     * <p>
     * Example: if the current position has a set of { 1, 4, 5, 9} as numbers that are already set
     * on that row, column or quadrant, the symmetric difference will be: { 2, 3, 6, 7, 8}
     *
     * @param set the numbers that are already present on row, column or quadrant for the current position
     * @return an array of integer elements that can possibly fit on that position
     */
    public int[] symmetricDifference(int[] set) {
        return Arrays.stream(allPossibilities).filter(possibleNumber ->
                Arrays.stream(set).noneMatch(element ->
                        element == possibleNumber
                )
        ).toArray();
    }

    /**
     * This method calculates the intersection between all sets (row, column and quadrant) for a specific
     * puzzle position.<br><br>
     *
     * Example: if row numbers are { 1, 2, 3, 4, 6 }, column numbers are { 2, 5, 8 }
     * and quadrant numbers are { 1, 2, 3, 7 } the intersection of these sets is
     * a set containing only the number 2, since 2 is the only number present on 3 sets.
     *
     * @param rowSet      row numbers
     * @param columnSet   column numbers
     * @param quadrantSet quadrant numbers
     * @return an array that contains the intersection between all 3 sets of possible numbers
     */
    public int[] getIntersection(int[] rowSet, int[] columnSet, int[] quadrantSet) {
        int[][] allSets = {rowSet, columnSet, quadrantSet};

        Optional<int[]> optionalValue = Arrays.stream(allSets).reduce((int[] accumulator, int[] currentSet) ->
                Arrays.stream(accumulator).filter(number ->
                        Arrays.stream(currentSet).anyMatch(currentSetNumber -> currentSetNumber == number)
                ).toArray()
        );

        return optionalValue.get();
    }

    /**
     * This method analyzes all possible numbers for a specific position and put an array with
     * all possible numbers into a 3x3 matrix that contains all possibilities for all positions.
     *
     * @param rowIndex    current position row index
     * @param columnIndex current position column index
     * @param row         current row numbers
     * @param column      current column numbers
     * @param quadrant    current quadrant numbers
     */
    public void analyzePossibilities(int rowIndex, int columnIndex, int[] row,
                                     int[] column, int[] quadrant) {
        int[] possibilitiesForRow = symmetricDifference(row);
        int[] possibilitiesForColumn = symmetricDifference(column);
        int[] possibilitiesForQuadrant = symmetricDifference(quadrant);

        int[] possibleNumbersForCurrentPosition = getIntersection(
                possibilitiesForRow, possibilitiesForColumn, possibilitiesForQuadrant
        );

        puzzlePossibilities[rowIndex][columnIndex] = possibleNumbersForCurrentPosition;
    }

    /**
     * This method checks all possible numbers for a specific position on puzzle.
     *
     * @param rowIndex    current row index
     * @param columnIndex current column index
     */
    public void checkCurrentPosition(int rowIndex, int columnIndex) {
        int[] currentRow = getCurrentRow(rowIndex);
        int[] currentColumn = getCurrentColumn(columnIndex);
        int[] currentQuadrant = getCurrentQuadrant(rowIndex, columnIndex);
        analyzePossibilities(rowIndex, columnIndex, currentRow, currentColumn, currentQuadrant);
        if (puzzlePossibilities[rowIndex][columnIndex].length == 1
                && Arrays.stream(puzzlePossibilities[rowIndex][columnIndex]).findFirst().isPresent()) {
            zerosInPuzzle -= 1;

            puzzle[rowIndex][columnIndex] =
                    Arrays.stream(puzzlePossibilities[rowIndex][columnIndex]).findFirst().getAsInt();
        }
    }

    public void solveEasyMethod() {
        for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < puzzle[rowIndex].length; columnIndex += 1) {
                if (puzzle[rowIndex][columnIndex] == 0) {
                    checkCurrentPosition(rowIndex, columnIndex);
                }
            }
        }
    }

    public void solveMediumMethod() {

    }

    public void solveHardMethod() {

    }

    public void solveVeryHardMethod() {

    }

    private void chooseSolverMethod() {
        if (zerosInPuzzle <= DifficultyZeros.EASY.value) {
            solveEasyMethod();
        } else if (zerosInPuzzle < DifficultyZeros.HARD.value && zerosInPuzzle >= DifficultyZeros.MEDIUM.value) {
            solveMediumMethod();
        } else if (zerosInPuzzle < DifficultyZeros.VERY_HARD.value && zerosInPuzzle >= DifficultyZeros.HARD.value) {
            solveHardMethod();
        } else {
            solveVeryHardMethod();
        }
    }

    public int[][] solve() {
        countZerosInPuzzle();
        startValidatingPuzzle();
        while (zerosInPuzzle > 0) {
            chooseSolverMethod();
        }
        return puzzle;
    }
}
