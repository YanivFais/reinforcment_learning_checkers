/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#ifndef __GAME__BOARD_H
#define __GAME__BOARD_H

#include "Game.h"
#include "Board.h"

/**
 * wrapper class dealing with Board as of size of board in Game
 */
class GameBoard
{
public:

	/**
	 * evaluate a board for Plater with color using weights and return values
	 * @param board Board for evaluation
	 * @param color color of evaluating Player
	 * @param weights weights used for evaluation
	 * @param values return values of evaluation parameters
	 * @return value of Board
	 */
	static int evalBoard(Board * board,char color,unsigned short* weights,int* values);

	/**
	 * make a move from this board
	 * @param initialBoard board for this
	 * @param move The move to perform
	 * @param color performing Player's color
	 * @return ParamBoard after move
	 */
	static Board* makeMove(Board* initialBoard,Move* move, char color);

	/**
	 * find all legal moves for player with color on this board
	 * @param initialBoard board for this
	 * @param color color of moving player
	 * @return all moves for player on this board
	 */
	static Moves * findLegalMoves(Board * initialBoard,char color);

	/**
	 * @param initialBoard board for this
	 * @return stage of game
	 */
	static int getStage(Board * board);


	static string getValue(Board * board);
};

#endif //__GAME__BOARD_H
