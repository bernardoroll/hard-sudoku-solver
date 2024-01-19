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
 * <p>
 * <b>NOTE:</b> We should try to use some more advanced techniques to solve any difficulty sudoku puzzle.
 * As mentioned by Chat-GPT these techniques can be:
 * <br>
 * <i>Apply various logical deductions such as "naked singles," "hidden singles," "naked pairs," "hidden pairs,"
 * "naked triples," and "hidden triples" to fill in more cells. These deductions involve analyzing the possible
 * values for cells in a row, column, or box to identify patterns and eliminate possibilities.</i>
 * </p>
 * <a href="https://github.com/Gretgor/HardSudokuSolver/blob/main/SudokuProblem.py">This is a sample of a Hard Sudoku
 * Solver using back track strategy.</a>
 */
public class SudokuSolver {

    private final Puzzle puzzle;

    private enum GivensInPuzzle {
        VERY_HARD(28),
        HARD(30),
        MEDIUM(32),
        EASY(32);

        private final static int NUMBER_OF_ELEMENTS_ON_PUZZLE = 81;
        private final int value;

        GivensInPuzzle(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }

        int zerosOnPuzzle() {
            return NUMBER_OF_ELEMENTS_ON_PUZZLE - value;
        }
    }

    private static final int MAX_UNKNOWN_POSITIONS_IN_PUZZLE = 64;

    private final int[] allPossibilities = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public SudokuSolver(int[][] grid) throws IllegalArgumentException {
        try {
            this.puzzle = new Puzzle(grid);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid grid puzzle. The grid must be an int[][] type.\nMessage: " +
                    exception.getMessage());
        }
    }

    /**
     * This method checks when the puzzle board has more than 9 rows or if any of its rows has
     * more than 9 elements on it and, at last it verifies if there is any invalid element on puzzle (e.g. an element
     * that is not a digit between 1 and 9).
     *
     * @throws IllegalArgumentException when any aforementioned conditions are found
     */
    private void startValidatingPuzzle() throws IllegalArgumentException {
        if (puzzle.size != 9) throw new IllegalArgumentException("It's not a 9x9 grid.");
        if (puzzle.zerosInPuzzle > MAX_UNKNOWN_POSITIONS_IN_PUZZLE) throw new IllegalArgumentException("Invalid puzzle. The " +
                "minimum of givens required to create an unique (with no multiple solutions) sudoku game is 17.");
        for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
            if (puzzle.getRowFromRowIndex(rowIndex).size() != 9) throw new IllegalArgumentException("The row at " + rowIndex +
                    " index has an invalid length.");
            for (int element : puzzle.getRowAsArray(rowIndex)) {
                if (element != 0 && (element < 1 || element > 9)) throw new IllegalArgumentException("At least one " +
                        "element is not a valid digit. Only digits from 1 to 9 are allowed.");
            }
        }
    }

    /**
     * This method builds an array with all current row elements, including the ones that are not set yet.
     * @param rowIndex the row index
     * @return an array with all elements of the row
     */
    public int[] getCurrentRow(int rowIndex) {
        return puzzle.getRowAsArray(rowIndex);
    }

    /**
     * This method builds an array with all current column elements, including the ones that are not set yet.
     * @param columnIndex the column index
     * @return an array with all elements of the column
     */
    public int[] getCurrentColumn(int columnIndex) {
//        return Arrays.stream(puzzle).mapToInt((row) -> row[columnIndex]).toArray();
        return puzzle.getColumnAsArray(columnIndex);
    }

    /**
     * This method calculates the 0-indexed number of the quadrant the row belongs to.
     * Example:
     * <br>
     * For a normal Sudoku puzzle (9x9), if row index is 7, this method will return 2:
     * Math.floor(7 / Math.sqrt(9)) = 2
     * @param rowIndex the position row index
     * @return the 0-indexed index the row belongs to (0, 1 or 2)
     */
    public int getRowQuadrant(int rowIndex) {
        return (int) Math.floor(rowIndex / Math.sqrt(puzzle.size));
    }

    /**
     * This method calculates the 0-indexed number of the quadrant the column belongs to.
     * Example:
     * <br>
     * For a normal Sudoku puzzle (9x9), if row index is 3, this method will return 1:
     * Math.floor(3 / Math.sqrt(9)) = 1
     * @param rowIndex the position row index
     * @param columnIndex the position column index
     * @return the 0-indexed index the column belongs to (0, 1 or 2)
     */
    public int getColumnQuadrant(int rowIndex, int columnIndex) {
        return (int) Math.floor(columnIndex / Math.sqrt(puzzle.size));
    }

    /**
     * This method returns the row index of the first (top-left) element from a specific quadrant.
     * @param rowQuadrant the 0-indexed index of the given quadrant
     * @return 0-indexed row index of the most top-left element
     */
    public int getCurrentQuadrantFirstRowIndex(int rowQuadrant) {
        return (int) (rowQuadrant * Math.sqrt(puzzle.size));
    }

    /**
     * @deprecated This method will be removed since it's no longer needed.
     * This method returns the column of the first (top-left) element form a specific quadrant.
     * @param columnQuadrant the 0-indexed index of the given column quadrant
     * @param rowIndex the 0-indexed element row index (this is for future use, since for a 9x9 puzzle it will never change)
     * @return 0-indexed column index of the most top-left element
     */
    @Deprecated
    public int getCurrentQuadrantFirstColumnIndex(int columnQuadrant, int rowIndex) {
        return (int) (columnQuadrant * Math.sqrt(puzzle.size));
    }

    /**
     * @deprecated This method will be removed since now we can access all elements from a given quadrant through each
     * {@link PuzzleElement} present on {@link Puzzle} member of this class. To access a PuzzleElement's quadrant you
     * only need to access its {@code quadrant} property through {@link PuzzleElement#quadrantIndex}.
     * <br>
     * This method builds a list of integers that contains all elements from a given quadrant.
     * @param currentQuadrantFirstRowPosition 0-indexed row index of the current quadrant
     * @param currentQuadrantFirstColumnPosition 0-indexed column index of the current quadrant
     * @return a list of integer with all elements of the current quadrant (including the ones that are not set yet)
     */
    @Deprecated
    public List<Integer> getCurrentQuadrantNumbers(int currentQuadrantFirstRowPosition,
                                                   int currentQuadrantFirstColumnPosition) {
        List<Integer> quadrantNumbers = new ArrayList<>();
        for (int i = 0; i < Math.sqrt(puzzle.size); i += 1) {
            for (int j = 0; j < Math.sqrt(puzzle.size); j += 1) {
                quadrantNumbers.add(
                        puzzle.getElementFromPosition(
                                currentQuadrantFirstRowPosition + i,
                                currentQuadrantFirstColumnPosition + j
                        ).getValue()
                );
            }
        }
        return quadrantNumbers;
    }

    /**
     * An auxiliary method to convert a list of integers into a primitive array of int.
     * @param list the list with integer elements
     * @return an array of int with all the list elements
     */
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

    /**
     * This method builds an array of int that contains all elements from a given quadrant.
     * @param rowIndex 0-indexed row index of the current quadrant
     * @param columnIndex 0-indexed column index of the current quadrant
     * @return an array of int with all elements of the current quadrant (including the ones that are not set yet)
     */
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

    private void generateCandidatesFor(PuzzleElement element) {
        int[] possibilitiesForRow = symmetricDifference(puzzle.getRowAsArray(element.rowIndex));
        int[] possibilitiesForColumn = symmetricDifference(puzzle.getColumnAsArray(element.columnIndex));
        int[] possibilitiesForQuadrant =
                symmetricDifference(puzzle.getQuadrantElementsValuesAsArray(element.quadrantIndex));

        int[] possibleNumbersForCurrentPosition = getIntersection(
                possibilitiesForRow, possibilitiesForColumn, possibilitiesForQuadrant
        );
        element.setCandidates(possibleNumbersForCurrentPosition);
    }

    /**
     * This method checks all possible numbers for a specific position on puzzle.
     *
     * @param rowIndex    current row index
     * @param columnIndex current column index
     */
    public void checkCurrentPosition(int rowIndex, int columnIndex) {
        PuzzleElement element = puzzle.getElementFromPosition(rowIndex, columnIndex);
        generateCandidatesFor(element);
        if (element.hasOnlyOnePossibleCandidate()) {
            element.setValueAndFlushCandidates(element.getCandidateWhenThereIsOnlyOne());
            puzzle.decreaseZerosInPuzzle();
        }
    }

    /**
     * @deprecated This method will be removed. Please use {@link PuzzleElement#isEmpty()} instead.
     * This is an auxiliary method to check if some position is still an empty cell (it has 0 as its value).
     * @param position {@link PositionData PositionData} with current position info
     * @return {@code true} if the current positions has an empty value (e.g. it has zero as its value) or
     * {@code false} otherwise
     */
    @Deprecated
    private boolean isPositionEmpty(PositionData position) {
        return puzzle.getElementFromPosition(position.rowIndex, position.columnIndex).getValue() == 0;
    }

    /**
     * An auxiliary method to verify if a set contains a given element.
     * @param set the set of elements
     * @param element element to be searched for on a set of elements
     * @return {@code true} if the element is present on the set, {@code false} otherwise
     */
    private boolean contains(int[] set, int element) {
        if (set.length == 0) return false;
        int index = 0;
        while (index < set.length) {
            if (set[index] == element) return true;
            index += 1;
        }
        return false;
    }

    /**
     * @deprecated This method will be removed since it's not necessary anymore.
     * @return an integer array to be used to find unique occurrences
     */
    @Deprecated
    private int[] truthTable() {
        return new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }

    /**
     * @deprecated This method will be removed and replaced for a more accurate one.
     * @param setsOfCandidates a matrix containing all the sets of candidates for a row
     * @return the 0-indexed index of the occurrence, -1 otherwise
     */
    @Deprecated
    private int findUniqueOccurrenceBetweenSets(int[][] setsOfCandidates) {
        int[] truthTable = truthTable();
        for (int[] set : setsOfCandidates) {
            for (int element : set) {
                if (element != 0) truthTable[element - 1] += 1;
            }
        }

        for (int index = 0; index < truthTable.length; index++) {
            if (truthTable[index] == 1) return index + 1;
        }
        return -1;
    }

    /**
     * @deprecated This method will be removed, since it's not actually searching for hidden singles on current
     * row.
     * @param position position related data
     */
    @Deprecated
    private void searchHiddenSinglesForRow(PositionData position) {
//        int[] row = puzzle[position.rowIndex];
//        int[][] rowWithCandidates = puzzlePossibilities[position.rowIndex];
//
//        for (int rowElement : row) {
//            if (rowElement == 0) {
//                for (int possibleCandidate : allPossibilities) {
//
//                }
//            }
//        }
//
//
//        for (int possibleCandidate : allPossibilities) {
//
//            int countMatchesForPossibleCandidate = 0;
//
//            for (int columnIndex = 0; columnIndex < puzzle.length; columnIndex += 1) {
//
//                int[] candidatesForPosition = puzzlePossibilities[position.rowIndex][columnIndex];
//
//                for (int candidate : candidatesForPosition) {
//                    if (candidate == possibleCandidate) {
//                        countMatchesForPossibleCandidate += 1;
//                    }
//                }
//
//                if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
//                    zerosInPuzzle -= 1;
//                    puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
//                }
//            }
//        }
    }

    /**
     * @deprecated This method will be removed, since it's not actually searching for hidden singles on current
     * column.
     * @param position position related data
     */
    @Deprecated
    private void searchHiddenSingleForColumn(PositionData position) {
//        for (int possibleCandidate : allPossibilities) {
//
//            int countMatchesForPossibleCandidate = 0;
//
//            for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
//
//                int[] candidatesForPosition = puzzlePossibilities[rowIndex][position.columnIndex];
//
//                for (int candidate : candidatesForPosition) {
//                    if (candidate == possibleCandidate) {
//                        countMatchesForPossibleCandidate += 1;
//                    }
//                }
//
//                if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
//                    zerosInPuzzle -= 1;
//                    puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
//                }
//            }
//        }
    }

    /**
     * @deprecated This method will be removed, since it's not actually searching for hidden singles on current
     * quadrant.
     * @param position position related data
     */
    @Deprecated
    private void searchHiddenSingleForQuadrant(PositionData position) {
//        for (int possibleCandidate : allPossibilities) {
//
//            int countMatchesForPossibleCandidate = 0;
//
//            int rowQuadrant = getRowQuadrant(position.rowIndex);
//            int columnQuadrant = getColumnQuadrant(position.rowIndex, position.columnIndex);
//            int currentQuadrantFirstRowPosition = getCurrentQuadrantFirstRowIndex(rowQuadrant);
//            int currentQuadrantFirstColumnPosition = getCurrentQuadrantFirstColumnIndex(columnQuadrant, position.rowIndex);
//
//            for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
//                for (int j = 0; j < Math.sqrt(puzzle[getCurrentQuadrantFirstRowIndex(rowQuadrant) + i].length); j += 1) {
//
//                    var candidates = puzzlePossibilities[currentQuadrantFirstRowPosition + i][currentQuadrantFirstColumnPosition + j];
//
//                    for (int candidate : candidates) {
//                        if (candidate == possibleCandidate) {
//                            countMatchesForPossibleCandidate += 1;
//                        }
//                    }
//
//                    if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
//                        zerosInPuzzle -= 1;
//                        puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
//                    }
//                }
//            }
//        }
    }

    /**
     * This method was implemented with Chat-GPT help. It provided the following pseudocode as an example.
     * function findHiddenPairs(grid):
     *     for each row in grid:
     *         countOccurrences = initialize empty dictionary
     *         for each cell in row:
     *             if cell is empty:
     *                 for each possibility in cell.possibilities:
     *                     countOccurrences[possibility] += 1
     *         hiddenPairs = find keys in countOccurrences with a value of 2
     *         if length of hiddenPairs is greater than or equal to 2:
     *             for each hiddenPair in hiddenPairs:
     *                 for each cell in row:
     *                     if cell is empty and hiddenPair is in cell.possibilities:
     *                         remove other possibilities from cell.possibilities except hiddenPair
     *     repeat the above steps for each column and each box
     *     return grid
     */
    private void findHiddenPairsInRows() {
        for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
            int[] countOccurrences = new int[puzzle.size];
            for (int columnIndex = 0; columnIndex < puzzle.size; columnIndex += 1) {
                PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                if (currentElement.isEmpty()) {
                    var candidates = currentElement.getCandidatesAsIntArray();
                    for (int candidate : candidates) {
                        countOccurrences[candidate - 1] += 1;
                    }
                }
            }
            int[] hiddenPairs = Arrays.stream(countOccurrences).filter(element -> element == 2).toArray();
            if (hiddenPairs.length >= 2) {
                for (int hiddenPair : hiddenPairs) {
                    for (int columnIndex = 0; columnIndex < puzzle.size; columnIndex += 1) {
                        PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                        boolean hiddenPairIsInCandidates = Arrays.stream(currentElement.getCandidatesAsIntArray())
                                .anyMatch(element -> element == hiddenPair);
                        if (currentElement.isEmpty() && hiddenPairIsInCandidates) {
                            currentElement.setCandidates(new int[] { hiddenPair });
                        }
                    }
                }
            }
        }
    }

    private void findHiddenPairsInColumns() {
        for (int columnIndex = 0; columnIndex < puzzle.size; columnIndex += 1) {
            int[] countOccurrences = new int[puzzle.size];
            for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
                PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                if (currentElement.isEmpty()) {
                    var candidates = currentElement.getCandidatesAsIntArray();
                    for (int candidate : candidates) {
                        countOccurrences[candidate - 1] += 1;
                    }
                }
            }
            int[] hiddenPairs = Arrays.stream(countOccurrences).filter(element -> element == 2).toArray();
            if (hiddenPairs.length >= 2) {
                for (int hiddenPair : hiddenPairs) {
                    for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
                        PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                        boolean hiddenPairIsInCandidates = Arrays.stream(currentElement.getCandidatesAsIntArray())
                                .anyMatch(element -> element == hiddenPair);
                        if (currentElement.isEmpty() && hiddenPairIsInCandidates) {
                            currentElement.setCandidates(new int[] { hiddenPair });
                        }
                    }
                }
            }
        }
    }

    private void findHiddenPairsInQuadrants() {
        for (int quadrantIndex = 0; quadrantIndex < puzzle.size; quadrantIndex += 1) {
            for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
                int[] countOccurrences = new int[puzzle.size];
                for (int columnIndex = 0; columnIndex < puzzle.size; columnIndex += 1) {
                    PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                    if (currentElement.quadrantIndex == quadrantIndex) {
                        if (currentElement.isEmpty()) {
                            var candidates = currentElement.getCandidatesAsIntArray();
                            for (int candidate : candidates) {
                                countOccurrences[candidate - 1] += 1;
                            }
                        }
                    }
                }
                int[] hiddenPairs = Arrays.stream(countOccurrences).filter(element -> element == 2).toArray();
                if (hiddenPairs.length >= 2) {
                    for (int hiddenPair : hiddenPairs) {
                        for (int columnIndex = 0; columnIndex < puzzle.size; columnIndex += 1) {
                            PuzzleElement currentElement = puzzle.getElementFromPosition(rowIndex, columnIndex);
                            if (currentElement.quadrantIndex == quadrantIndex) {
                                boolean hiddenPairIsInCandidates = Arrays.stream(currentElement.getCandidatesAsIntArray())
                                        .anyMatch(element -> element == hiddenPair);
                                if (currentElement.isEmpty() && hiddenPairIsInCandidates) {
                                    currentElement.setCandidates(new int[] { hiddenPair });
                                }
                            }
                        }
                    }
                }
            }
        }
    }




    /**
     * This method solves easy sudoku puzzles via brute force strategy.
     */
    public void tryToSolveUsingBruteForce() {
        for (int rowIndex = 0; rowIndex < puzzle.size; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < puzzle.getRowAsArray(rowIndex).length; columnIndex += 1) {
                if (puzzle.getElementFromPosition(rowIndex, columnIndex).isEmpty()) {
                    checkCurrentPosition(rowIndex, columnIndex);
                }
            }
        }
    }

    private void applyMethodsToSolvePuzzle() {
        tryToSolveUsingBruteForce();
        findHiddenPairsInRows();
        findHiddenPairsInColumns();
        findHiddenPairsInQuadrants();
    }

    public int[][] solve() {
        if (this.puzzle == null) throw new IllegalArgumentException("Puzzle cannot be null.");
        puzzle.setInitialZerosInPuzzle();
        startValidatingPuzzle();
        while (puzzle.zerosInPuzzle > 0) {
            applyMethodsToSolvePuzzle();
        }
        return puzzle.convertToGrid();
    }
}

class Candidate {
    int value;
    boolean isAHiddenSingle;
    boolean isPartOfHiddenPair;
    boolean isPartOfHiddenTriple;
    boolean isANakedSingle;
    boolean isPartOfNakedPair;
    boolean isPartOfNakedTriple;

    Candidate(int value) {
        this.value = value;
    }

}

class PuzzleElement {

    int rowIndex;
    int columnIndex;
    int quadrantIndex;
    private int value;
    private List<Candidate> candidates;

    boolean hasOnlyOnePossibleCandidate() {
        return candidates.size() == 1;
    }

    int getCandidateWhenThereIsOnlyOne() {
        if (hasOnlyOnePossibleCandidate()) return candidates.get(0).value;
        return -1;
    }

    PuzzleElement(int rowIndex, int columnIndex, int value) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
        this.candidates = new ArrayList<>();
        calculateAndSetPositionQuadrant();
    }

    /**
     * This method calculates the 0-indexed index of the position's quadrant. This info will be saved in
     * {@link PuzzleElement#quadrantIndex} property. Below there is kind of a table used to explore and to define this
     * equation.
     * <br>
     * Quadrant 0 -> (0,0) (0,1) (0,2) Quadrant 1 -> (0,3) (0,4) (0,5) Quadrant 2 -> (0,6) (0,7) (0,8) |
     * Quadrant 0 -> (1,0) (1,1) (1,2) Quadrant 1 -> (1,3) (1,4) (1,5) Quadrant 2 -> (1,6) (1,7) (1,8) | -> + 0
     * Quadrant 0 -> (2,0) (2,1) (2,2) Quadrant 1 -> (2,3) (2,4) (2,5) Quadrant 2 -> (2,6) (2,7) (2,8) |
     * <br>
     * Quadrant 3 -> (3,0) (4,1) (3,2) Quadrant 4 -> (3,3) (3,4) (3,5) Quadrant 5 -> (3,6) (3,7) (3,8) |
     * Quadrant 3 -> (4,0) (4,1) (4,2) Quadrant 4 -> (4,3) (4,4) (4,5) Quadrant 5 -> (4,6) (4,7) (4,8) | -> + 2
     * Quadrant 3 -> (5,0) (5,1) (5,2) Quadrant 4 -> (5,3) (5,4) (5,5) Quadrant 5 -> (5,6) (5,7) (5,8) |
     * <br>
     * Quadrant 6 -> (6,0) (6,1) (6,2) Quadrant 7 -> (6,3) (6,4) (6,5) Quadrant 8 -> (6,6) (6,7) (6,8) |
     * Quadrant 6 -> (7,0) (7,1) (7,2) Quadrant 7 -> (7,3) (7,4) (7,5) Quadrant 8 -> (7,6) (7,7) (7,8) | -> + 4
     * Quadrant 6 -> (8,0) (8,1) (8,2) Quadrant 7 -> (8,3) (8,4) (8,5) Quadrant 8 -> (8,6) (8,7) (8,8) |
     * <br>
     * For rows from 0 to 2
     * Math.floor(0..2 / Math.sqrt(9)) + Math.floor(columnIndex / Math.sqrt(9)) + 0
     * For rows from 3 to 5
     * Math.floor(3..5 / Math.sqrt(9)) + Math.floor(columnIndex / Math.sqrt(9)) + 2
     * For rows from 6 to 8
     * Math.floor(6..8 / Math.sqrt(9)) + Math.floor(columnIndex / Math.sqrt(9)) + 4
     */
    private void calculateAndSetPositionQuadrant() {
        // TODO: This formulae can be improved to be more flexible and reusable
        this.quadrantIndex = this.rowIndex / 3 * 3 + this.columnIndex / 3;
    }

    /**
     * This method sets the value for this {@link PuzzleElement} only, and only if, the current value is set to 0,
     * which means that the current element hasn't been set yet. It throws a {@link RuntimeException} otherwise.
     * @param value the value to be set to current element
     * @throws RuntimeException it throws an exception if this value isn't empty (e.g. this value is not equals zero)
     */
    public void setValueAndFlushCandidates(int value) throws RuntimeException {
        if (isNotEmpty()) throw new RuntimeException("This value has already been set.");
        this.value = value;
        this.candidates = new ArrayList<>();
    }

    public int getValue() {
        return this.value;
    }

    /**
     * @deprecated Use {@link PuzzleElement#getCandidatesAsIntArray()} instead.
     * @return
     */
    @Deprecated
    public int[] getCandidatesAsArray() {
        int[] candidatesAsArray = new int[candidates.size()];
        for (int index = 0; index < candidates.size(); index += 1) {
            candidatesAsArray[index] = candidates.get(index).value;
        }
        return candidatesAsArray;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public int[] getCandidatesAsIntArray() {
        return candidates.stream().mapToInt(candidate -> candidate.value).toArray();
    }

    public void setCandidates(int[] candidates) {
        this.candidates = new ArrayList<>();
        for (int candidate : candidates) {
            this.candidates.add(new Candidate(candidate));
        }
    }

    boolean isEmpty() { return this.value == 0; }

    boolean isNotEmpty() { return this.value != 0; }
}

class Puzzle {

    int zerosInPuzzle = 0;
    public int size;
    List<List<PuzzleElement>> puzzle;

    Puzzle(int[][] grid) {
        requireNotNullOrEmpty(grid);
        puzzle = new ArrayList<>();
        this.size = grid.length;
        for (int rowIndex = 0; rowIndex < this.size; rowIndex += 1) {
            var newRow = new ArrayList<PuzzleElement>();
            for (int columnIndex = 0; columnIndex < this.size; columnIndex += 1) {
                PuzzleElement puzzleElement = new PuzzleElement(rowIndex, columnIndex, grid[rowIndex][columnIndex]);
                newRow.add(puzzleElement);
            }
            puzzle.add(newRow);
        }
    }

    PuzzleElement getElementFromPosition(int rowIndex, int columnIndex) {
        return puzzle.get(rowIndex).get(columnIndex);
    }

    List<PuzzleElement> getRowFromRowIndex(int rowIndex) {
        return puzzle.get(rowIndex);
    }

    /**
     * Returns a list with all elements from a given quadrant.
     * @param index 0-indexed index of the quadrant
     * @return a list with all {@link PuzzleElement}s from a given quadrant
     */
    List<PuzzleElement> getQuadrantElements(int index) {
        List<PuzzleElement> quadrantElements = new ArrayList<>();
        for (List<PuzzleElement> row : puzzle) {
            for (PuzzleElement element : row) {
                if (element.quadrantIndex == index) {
                    quadrantElements.add(element);
                }
            }
        }
        return quadrantElements;
    }

    int[] getQuadrantElementsValuesAsArray(int index) {
        int[] resultArray = new int[puzzle.size()];
        List<PuzzleElement> quadrantPuzzleElements = getQuadrantElements(index);
        for (int elementIndex = 0; elementIndex < quadrantPuzzleElements.size(); elementIndex += 1) {
            resultArray[elementIndex] = quadrantPuzzleElements.get(elementIndex).getValue();
        }
        return resultArray;
    }

    List<PuzzleElement> getColumnFromColumnIndex(int columnIndex) {
        List<PuzzleElement> columnElements = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < this.size; rowIndex += 1) {
            columnElements.add(puzzle.get(rowIndex).get(columnIndex));
        }
        return columnElements;
    }

    int[] getRowAsArray(int rowIndex) {
        int[] rowAsArray = new int[this.size];
        for (int currentColumnIndex = 0; currentColumnIndex < this.size; currentColumnIndex += 1) {
            rowAsArray[currentColumnIndex] = puzzle.get(rowIndex).get(currentColumnIndex).getValue();
        }
        return rowAsArray;
    }

    int[] getColumnAsArray(int columnIndex) {
        int[] columnAsArray = new int[this.size];
        for (int currentRowIndex = 0; currentRowIndex < this.size; currentRowIndex += 1) {
            columnAsArray[currentRowIndex] = puzzle.get(currentRowIndex).get(columnIndex).getValue();
        }
        return columnAsArray;
    }

    void setInitialZerosInPuzzle() {
        for (int rowIndex = 0; rowIndex < this.size; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < this.size; columnIndex += 1) {
                if (getElementFromPosition(rowIndex, columnIndex).isEmpty()) zerosInPuzzle += 1;
            }
        }
    }

    int[][] convertToGrid() {
        int[][] grid = new int[this.size][this.size];
        for (int rowIndex = 0; rowIndex < this.size; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < this.size; columnIndex += 1) {
                grid[rowIndex][columnIndex] = puzzle.get(rowIndex).get(columnIndex).getValue();
            }
        }
        return grid;
    }

    void decreaseZerosInPuzzle() {
        this.zerosInPuzzle -= 1;
    }

    private void requireNotNullOrEmpty(int[][] grid) throws IllegalArgumentException {
        if (grid == null) throw new IllegalArgumentException("Puzzle grid cannot be null.");
        if (grid.length == 0) throw new IllegalArgumentException("Puzzle cannot be empty.");
        for (int[] row : grid) {
            if (row.length == 0) throw new IllegalArgumentException("Puzzle cannot contain empty rows");
        }
    }
}

@Deprecated
class PositionData {
    int rowIndex;
    int columnIndex;
    int[] row;
    int[] column;
    int[] quadrant;

    /**
     * PositionData constructor that receives all the data related to the current position
     * @param rowIndex current position row index
     * @param columnIndex current position column index
     * @param row current row numbers
     * @param column current column numbers
     * @param quadrant current quadrant numbers
     */
    PositionData(int rowIndex, int columnIndex, int[] row, int[] column, int[] quadrant) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.row = row;
        this.column = column;
        this.quadrant = quadrant;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int[] getRow() {
        return row;
    }

    public void setRow(int[] row) {
        this.row = row;
    }

    public int[] getColumn() {
        return column;
    }

    public void setColumn(int[] column) {
        this.column = column;
    }

    public int[] getQuadrant() {
        return quadrant;
    }

    public void setQuadrant(int[] quadrant) {
        this.quadrant = quadrant;
    }
}
