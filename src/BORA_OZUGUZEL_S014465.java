// package project3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class BORA_OZUGUZEL_S014465 {

	public static final String FILE_NAME = "Input_BORA_OZUGUZEL_S014465.txt";
	public static String filePath;

	public static void main(String[] args) {
		try {
			TuringMachine tm = new TuringMachine(readFile(FILE_NAME));
			// System.out.println(tm);
			int lowerTreshold = 35;
			int upperTreshold = 100;
			String result = tm.getRouteAndResult(lowerTreshold, upperTreshold);
			System.out.println(result);

		} catch (NullPointerException e) {
			System.out.println("NullPointerException");
		}
	}

	public static Scanner readFile(String fileName) {
		try {
			File file = new File(fileName);
			if (file.exists()) {
				filePath = file.getAbsolutePath();
			} else {
				findFile(FILE_NAME, new File("."));
			}
			return new Scanner(new File(filePath));
		} catch (FileNotFoundException e) {
			System.out.println("Exception.");
		}
		return null;
	}

	public static void findFile(String fileName, File dir) {
		File[] list = dir.listFiles();
		if (list != null) {
			for (File f : list) {
				if (f.isDirectory()) {
					findFile(fileName, f);
				} else if (fileName.equals(f.getName())) {
					filePath = f.getAbsolutePath();
				}
			}
		}
	}
}

class TuringMachine {
	List<Character> inputAlphabet;
	List<Character> tapeAlphabet;
	char blankSymbol;
	List<String> states;
	String startState;
	String acceptState;
	String rejectState;
	List<Transition> transitions;
	String stringToBeDetected;

	public TuringMachine(Scanner sc) {
		this.inputAlphabet = new ArrayList<Character>();
		this.tapeAlphabet = new ArrayList<Character>();
		this.states = new ArrayList<String>();
		this.transitions = new ArrayList<Transition>();
		if (sc.hasNextLine()) {
			int numberOfInputVaribles = Integer.parseInt(sc.nextLine());
			StringTokenizer inputAlphabetSt = new StringTokenizer(sc.nextLine(), " ");
			while (inputAlphabetSt.hasMoreTokens() && this.inputAlphabet.size() < numberOfInputVaribles) {
				this.inputAlphabet.add(inputAlphabetSt.nextToken().charAt(0));
			}

			int numberOfTapeVaribles = Integer.parseInt(sc.nextLine());
			StringTokenizer tapeAlphabetSt = new StringTokenizer(sc.nextLine(), " ");
			while (tapeAlphabetSt.hasMoreTokens() && this.tapeAlphabet.size() < numberOfTapeVaribles) {
				this.tapeAlphabet.add(tapeAlphabetSt.nextToken().charAt(0));
			}

			this.blankSymbol = sc.nextLine().charAt(0);
			int numberOfStates = Integer.parseInt(sc.nextLine());
			StringTokenizer statesSt = new StringTokenizer(sc.nextLine(), " ");
			while (statesSt.hasMoreTokens() && this.states.size() < numberOfStates) {
				this.states.add(statesSt.nextToken());

			}

			this.startState = sc.nextLine();
			this.acceptState = sc.nextLine();
			this.rejectState = sc.nextLine();

			String currentLine;
			while (sc.hasNextLine()) {
				currentLine = sc.nextLine();
				if (!sc.hasNextLine()) {
					this.stringToBeDetected = currentLine;
					break;
				} else {
					StringTokenizer st = new StringTokenizer(currentLine, " ");
					if (st.hasMoreTokens()) {
						String transitionFromState = st.nextToken();
						char oldSymbol = st.nextToken().charAt(0);
						char newSymbol = st.nextToken().charAt(0);
						String moveAction = st.nextToken();
						String transitionToState = st.nextToken();
						this.transitions.add(new Transition(transitionFromState, oldSymbol, newSymbol, moveAction,
								transitionToState));
					}

				}
			}
		}
	}

	public String getRouteAndResult(int lowerTreshold, int upperTreshold) {
		List<String> route = new ArrayList<String>();
		String tmResult = "";
		String tape = this.stringToBeDetected;
		int initialTapeLength = tape.length();
		int headLocation = 0;
		List<String> history = new ArrayList<String>();
		String currentState = this.startState;

		while (true) {
			String currentHistory = "" + tape + currentState + headLocation;
			// System.out.println(currentHistory);
			if (currentState.equals(this.acceptState)) {
				route.add(currentState);
				tmResult = "accepted";
				break;
			} else if (currentState.equals(this.rejectState)) {
				route.add(currentState);
				tmResult = "rejected";
				break;
			} else if (history.contains(currentHistory) && history.size() > lowerTreshold
					|| history.size() == upperTreshold
					|| tape.length() - initialTapeLength > this.states.size() * this.tapeAlphabet.size()
							&& history.size() > lowerTreshold) {
				route.add(currentState);
				tmResult = "looped";
				break;
			}

			history.add(currentHistory);

			final String tempCurrentState = currentState;
			final int tempHeadLocation = headLocation;
			final String tempTape = tape;
			Optional<Transition> optionalTransition = this.transitions.stream()
					.filter(t -> t.transitionFromState.equals(tempCurrentState)
							&& t.oldSymbol == getSymbolFromTape(tempTape, tempHeadLocation))
					.findFirst();
			if (optionalTransition.isEmpty()) {
				route.add(currentState);
				tmResult = "rejected";
				break;
			}
			Transition currentTransition = optionalTransition.get();
			if (tempTape.length() == headLocation) {
				tape += "" + currentTransition.newSymbol;
			} else if (tempTape.length() > headLocation) {
				tape = "" + tempTape.substring(0, headLocation) + currentTransition.newSymbol;
				if (tempTape.length() > headLocation + 1) {
					tape += tempTape.substring(headLocation + 1);
				}
			}

			if (currentTransition.moveAction.equals("L") && headLocation != 0) {
					headLocation--;
			} else if (currentTransition.moveAction.equals("R")) {
				headLocation++;
			}
			// System.out.println(tape);
			route.add(currentState);

			currentState = currentTransition.transitionToState;

		}

		String result = "ROUT: ";
		boolean firstState = true;
		for (String state : route) {
			if (firstState) {
				result += state;
				firstState = false;
			} else {
				result += " " + state;
			}
		}
		return result + "\nRESULT: " + tmResult;
	}

	public char getSymbolFromTape(String tape, int headLocation) {
		if (tape.length() == 0 || tape.length() <= headLocation) {
			return this.blankSymbol;
		} else {
			return tape.charAt(headLocation);
		}
	}

	@Override
	public String toString() {
		String result = "" + this.inputAlphabet.size() + "\n";
		boolean firstInputVarible = true;
		for (char inputVariable : this.inputAlphabet) {
			if (firstInputVarible) {
				result += inputVariable;
				firstInputVarible = false;
			} else {
				result += " " + inputVariable;
			}
		}
		result += "\n" + this.tapeAlphabet.size() + "\n";
		boolean firstTapeVarible = true;
		for (char tapeVariable : this.tapeAlphabet) {
			if (firstTapeVarible) {
				result += tapeVariable;
				firstTapeVarible = false;
			} else {
				result += " " + tapeVariable;
			}
		}
		result += "\n" + this.blankSymbol + "\n" + this.states.size() + "\n";
		boolean firstState = true;
		for (String state : this.states) {
			if (firstState) {
				result += state;
				firstState = false;
			} else {
				result += " " + state;
			}
		}
		result += "\n" + this.startState + "\n" + this.acceptState + "\n" + this.rejectState + "\n";
		for (Transition transition : this.transitions) {
			result += transition + "\n";
		}
		return result + this.stringToBeDetected;
	}

}

class Transition {
	String transitionFromState;
	char oldSymbol;
	char newSymbol;
	String moveAction;
	String transitionToState;

	public Transition(String transitionFromState, char oldSymbol, char newSymbol, String moveAction,
			String transitionToState) {
		this.transitionFromState = transitionFromState;
		this.oldSymbol = oldSymbol;
		this.newSymbol = newSymbol;
		this.moveAction = moveAction;
		this.transitionToState = transitionToState;
	}

	@Override
	public String toString() {
		return this.transitionFromState + " " + this.oldSymbol + " " + this.newSymbol + " " + this.moveAction + " "
				+ this.transitionToState;
	}

}