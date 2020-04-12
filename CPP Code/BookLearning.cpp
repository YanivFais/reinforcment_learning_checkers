/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/


#include <bitset>
#include <time.h>
#include <fstream>
#include "Game.h"
#include "GameBoard.h"
#include "Board.h"
#include "Book.h"


#define DEFAULT_LEVEL 8
#define DEFAULT_STEPS 3

using namespace std;

/**
 * Makes a move for book preparation
 * @param board current board
 * @param depth number of moves already played by strong player
 * @param turn color of player to play
 * @param steps limit on depth
 */
void playForBook(Board* board, int depth, char turn, int steps)
{
	Game* game = Game::getInstance();
	game->setCurrentBoard(board);
	if (GameBoard::getStage(board) == STAGES-1 && depth < steps)
	{		
		if (game->getPlayer(turn)->getLevel()) // strong player writes book
		{
			++depth;		
			Book* book = game->getBook();
			Move *move = book->getBookMove(board, turn);
			if (!move)
			{
				move = game->getPlayer(turn)->play();
				if (move) book->addBookMove(board, turn, move);
			}
			if (move)
			{
				Board* newBoard = GameBoard::makeMove(board, move, turn);
				playForBook(newBoard, depth, opp(turn), steps);
				delete move;
				delete newBoard;
			}
		}
		else // weak player makes all possible moves
		{ 			
			Moves* moves = GameBoard::findLegalMoves(board, turn);
			if (! moves->size())
			{
				delete moves;
				return;
			}
			for (Moves::iterator it = moves->begin(); it != moves->end(); ++it)
			{
				Move* move = *it;				
				Board* newBoard = GameBoard::makeMove(board, move, turn);
				playForBook(newBoard, depth, opp(turn), steps);
				delete move;
				delete newBoard;
			}
			delete moves;
		}
	}
	return;
}


/**
 * Creates opening book
 * @param boardSize board representation length in bits
 * @param level book level
 * @param steps number of game steps (moves of book player)
 */
void createBook(int boardSize, char level, int steps)
{
	// Clear existing book
	char fname[30];
	sprintf(fname, "%s%s%s%d%s",DATA_DIRECTORY,DIR_DELIMITER,BOOK_FILE_PREFIX_NAME, boardSize,BOOK_FILE_EXTENSION);
	ofstream ofs;
	ofs.open(fname, ios::out | ios::binary);
	ofs.write(&level, 1);
	ofs.close();
	Game* game = Game::getInstance();


	game->setBoardSize(boardSize);
	Board* board;

	// Initialize board	
	int i;
	switch (boardSize)
	{
	case 32:
		{
			bitset<32> whitePeons, blackPeons;
			for (i=0; i<12; i++)
			{
				whitePeons.set(i);
				blackPeons.set(32-1-i);
			}
			board = new ParamBoard<32>(whitePeons, blackPeons);
			break;
		}
	case 18:
		{
			bitset<18> whitePeons, blackPeons;
			for (i=0; i<6; i++)
			{
				whitePeons.set(i);
				blackPeons.set(18-1-i);
			}
			board = new ParamBoard<18>(whitePeons, blackPeons);
			break;
		}
	case 50:
		{
			bitset<50> whitePeons, blackPeons;
			for (i=0; i<20; i++)
			{
				whitePeons.set(i);
				blackPeons.set(50-1-i);
			}
			board = new ParamBoard<50>(whitePeons, blackPeons);
			break;
		}
	}
	game->setCurrentBoard(board);

	// Prepare book, set white as strong player
	// (i.e., book for the starting player)
	game->getPlayer(COLOR_WHITE)->setLevel(level);
	game->getPlayer(COLOR_BLACK)->setLevel(0);

	playForBook(board, 0, COLOR_WHITE, steps);

	// Prepare book, set black as strong player
	// (i.e., book for the non-starting player)
	game->getPlayer(COLOR_WHITE)->setLevel(0);
	game->getPlayer(COLOR_BLACK)->setLevel(level);

	//playForBook(board, 0, COLOR_WHITE, steps);

	delete board;
}


/**
 * display help "screen"
 */
void displayBookHelp()
{
	cout <<	"Usage: book [-6] [-8] [-10] [-level <l>] [-steps <s>] [-h] \n\n"
			<< "Where: \n"
			<< "      [-6 | -8 | -10]: board sizes to prepare book for (DEFAULT:all)\n\n"
			<< "      -level <l>: set book level,\n"
			<< "         where l is integral number of book level\n"
			<< "         (min-max tree depth) (DEFAULT:8)\n\n"
			<< "      -steps <s>: set number of steps (DEFAULT:3)\n\n"
			<< "      -h: To display this help screen\n\n";
}

/**
 * parse book application parameters
 * @param argc number of arguments
 * @param argv arguments
 * @param book18 reference indicator to be set if training 6x6 board
 * @param book32 reference indicator to be set if training 8x8 board
 * @param book50 reference indicator to be set if training 10x10 board
 * @param level reference to be set with level book
 * @param steps reference to be set with number of book steps
 */
int parseBookArguments(int argc,char *argv[],bool&book18,bool&book32,bool&book50,
						   int&level,int&steps)
{
	for (int i=1;i<argc;i++)
	{
		if (strcmp(argv[i],"-6")==0)
			book18 = true;
		else if (strcmp(argv[i],"-8")==0)
			book32 = true;
		else if (strcmp(argv[i],"-10")==0)
			book50 = true;
		else if (strcmp(argv[i],"-level")==0) {
			if (++i<argc)
				level = atoi(argv[i]);
			if ((i>=argc) | (level==0)) {
				cerr << "error: setting level,see help\n";
				return 1;
			}
		}
		else if (strcmp(argv[i],"-steps")==0) {
			if (++i<argc)
				steps = atoi(argv[i]);
			if ((i>=argc) | (steps==0)) {
				cerr << "error: setting steps,see help\n";
				return 2;
			}
		}
		else if  (strcmp(argv[i],"-h")==0) {
			displayBookHelp();
			return 5;
		}
		else {
			cerr << "Unknown command: " << argv[i] << ",see help\n";
			return 4;
		}
	}
	return 0;
}

/**
 * Main for book writer
 * @param argc number of arguments
 * @param argv arguments
 */
int main(int argc, char **argv)
{
	cout << "Dam Ka! Opening Book Writer\n\n";
	bool book32 = false, book18 = false,book50 = false,CSV=false;;
	int level = DEFAULT_LEVEL;
	int steps = DEFAULT_STEPS;

	// handle input parameters
	if (argc ==1 )
		displayBookHelp();
	else
	{
		int parse = parseBookArguments(argc,argv,book18,book32,book50,level,steps);
		if (parse)
			return parse;
	}

	cout << "\nExisting Opening books might be lost!";
	cout << "\nUse "<< argv[0] <<" -h for more information.\n";
	cout << "\nPress ENTER to start...\n\n";
	getchar();


	if (!book18 & !book32 &!book50)
		book18 = book32 = book50 = true;

	/**
	 * prepare books for different board sizes in level and number of stages
	 */
	if (book18)
	{
		cout << "Writing Book for 6x6 Board, " << steps << " steps, level " << level << endl;
		createBook(18, level, steps);
		cout << "Book is ready.\n\n";
	}

	if (book32)
	{
		cout << "Writing Book for 8x8 Board, " << steps << " steps, level " << level << endl;
		createBook(32, level, steps);
		cout << "Book is ready.\n\n";
	}
	if (book50)
	{
		cout << "Writing Book for 10x10 Board, " << steps << " steps, level " << level << endl;
		createBook(50, level, steps);
		cout << "Book is ready.\n\n";
	}
	cout << "Book writing is over.\n";
	return 0;
}








