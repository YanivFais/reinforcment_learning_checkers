/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#include "GameBoard.h"

/**
 * static definitions of special squares
 */
char ParamBoard<32>::centerField[8] = {9,10,13,14,17,18,21,22};
char ParamBoard<18>::centerField[8] = {4,5,6,7,10,11,12,13};
char ParamBoard<50>::centerField[8] = {17,18,21,22,28,28,32,33};


char ParamBoard<32>::doubleCorners[2] = {3,28};
char ParamBoard<18>::doubleCorners[2] = {2,15};
char ParamBoard<50>::doubleCorners[2] = {4,45};

char ParamBoard<32>::otherNearCrampWhite[4] = {16,20,21,24};
char ParamBoard<18>::otherNearCrampWhite[4] = {9,10,12,13};
char ParamBoard<50>::otherNearCrampWhite[4] = {20,25,26,31};

char ParamBoard<32>::otherNearCrampBlack[4] = {7,10,11,15};
char ParamBoard<18>::otherNearCrampBlack[4] = {4,5,7,9};
char ParamBoard<50>::otherNearCrampBlack[4] = {19,23,24,29};

char ParamBoard<32>::nearCrampWhite[2] = {18,23};
char ParamBoard<18>::nearCrampWhite[2] = {10,13};
char ParamBoard<50>::nearCrampWhite[2] = {33,39};

char ParamBoard<32>::nearCrampBlack[2] = {8,13};
char ParamBoard<18>::nearCrampBlack[2] = {4,7};
char ParamBoard<50>::nearCrampBlack[2] = {10,16};

char ParamBoard<32>::triangleOfOreoWhite[3] = {25,29,30};
char ParamBoard<18>::triangleOfOreoWhite[3] = {13,16,17};
char ParamBoard<50>::triangleOfOreoWhite[3] = {42,46,48};

char ParamBoard<32>::triangleOfOreoBlack[3] = {1,2,6};
char ParamBoard<18>::triangleOfOreoBlack[3] = {0,1,4};
char ParamBoard<50>::triangleOfOreoBlack[3] = {1,3,7};

char ParamBoard<32>::backRowBridgeWhiteSquares[2] = {0,2};
char ParamBoard<18>::backRowBridgeWhiteSquares[2] = {0,1};
char ParamBoard<50>::backRowBridgeWhiteSquares[2] = {0,3};

char ParamBoard<32>::backRowBridgeBlackSquares[2] = {29,31};
char ParamBoard<18>::backRowBridgeBlackSquares[2] = {16,17};
char ParamBoard<50>::backRowBridgeBlackSquares[2] = {46,49};

char ParamBoard<32>::whiteCrampingSquare = 12;
char ParamBoard<18>::whiteCrampingSquare = 6;
char ParamBoard<50>::whiteCrampingSquare = 15;

char ParamBoard<32>::blackCrampingSquare = 19;
char ParamBoard<18>::blackCrampingSquare = 11;
char ParamBoard<50>::blackCrampingSquare = 34;

// board size
char ParamBoard<32>::size = 8;
char ParamBoard<18>::size = 6;
char ParamBoard<50>::size = 10;

// make a move from board
Board* GameBoard::makeMove(Board* initialBoard,Move* move, char color)
{
	switch (Game::getInstance()->getBoardSize())
	{
		case 18:
			return((ParamBoard<18>*)initialBoard)->makeMove(move, color);
		case 32:
			return((ParamBoard<32>*)initialBoard)->makeMove(move, color);
		case 50:
			return ((ParamBoard<50>*)initialBoard)->makeMove(move, color);
	}
	return NULL;
}

// find legal moves from board to player with color
Moves * GameBoard::findLegalMoves(Board * initialBoard,char color)
{
	switch (Game::getInstance()->getBoardSize())
	{
	case 18:
		return((ParamBoard<18>*)initialBoard)->findLegalMoves(color);
	case 32:
		return((ParamBoard<32>*)initialBoard)->findLegalMoves(color);
	case 50:
		return ((ParamBoard<50>*)initialBoard)->findLegalMoves(color);
	}
	return NULL;
}

// get stage in game of board
int GameBoard::getStage(Board * initialBoard)
{
	switch (Game::getInstance()->getBoardSize())
	{
		case 18:
			return((ParamBoard<18>*)initialBoard)->getStage();
		case 32:
			return((ParamBoard<32>*)initialBoard)->getStage();
		case 50:
			return ((ParamBoard<50>*)initialBoard)->getStage();
	}
	return 0;
}

// evaluate a board
int GameBoard::evalBoard(Board * board,char color,unsigned short* weights,int* values)
{
	switch (Game::getInstance()->getBoardSize())
	{
		case 18:
			return ((ParamBoard<18>*)board)->eval(color, weights, values);
			break;
		case 32:
			return ((ParamBoard<32>*)board)->eval(color, weights, values);
			break;
		case 50:
			return ((ParamBoard<50>*)board)->eval(color, weights, values);
			break;
	}
	return 0;
}

// evaluate a board
string GameBoard::getValue(Board * board)
{
	switch (Game::getInstance()->getBoardSize())
	{
		case 18:
			return ((ParamBoard<18>*)board)->getBoardValue();
			break;
		case 32:
			return ((ParamBoard<32>*)board)->getBoardValue();
			break;
		case 50:
			return ((ParamBoard<50>*)board)->getBoardValue();
			break;
	}
	return 0;
}