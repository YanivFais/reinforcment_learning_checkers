/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#ifndef __PLAYER_H
#define __PLAYER_H

#include "Checkers.h"
#include "Move.h"

#include <vector>
#include <map>
#include <bitset>
#include <limits>


using namespace std;

class Board;
struct LessBitset;

/**
 * moves as list of Move
 */
typedef list<Move*> Moves;




/**
 * Represents a Player in the checkers Game
 */
class Player
{
	private:


		// weights of parametes vector
		unsigned short **weights;

		/**
		 * level of player
		 */
		char _level;

		/**
		 * color of player
		 */
		char _color;

		/**
		 * opponent color
		 */
		char _oppColor;

		/**
		 * the last move's board (for learning)
		 */
		Board* _lastBoard;

		/**
		 * the last move's expected value (for learning)
		 */
		double _lastExpectedValue;

		/**
		 * the last move played (for learning)
		 */
		Move* _lastMove;

	   /**
		* eval a board
		* @param board Board for evaluating
		* @return evaluation number
		*/
		int evalBoard(Board* board);


	   /**
		* eval a board with returned values
		* @param board Board for evaluating
		* @param values values of parameters, to be set
		* @return evaluation number
		*/
		int evalBoard(Board* board , int* values);

		/**
		 * eval tree and return maximum leaf
		 * @param board starting board
		 * @param moves starting moves
		 * @param depth depth in tree
		 * @param alpha lower window value
		 * @param beta upper window value
		 * @param initialVal initial value for leaf
		 */
		int evalMax(Board* board, Moves* moves,  char  depth, int alpha, int beta, int initialVal);

		/**
		 * eval tree and return minimum leaf
		 * @param board starting board
		 * @param moves starting moves
		 * @param depth depth in tree
		 * @param alpha lower window value
		 * @param beta upper window value
		 * @param initialVal initial value for leaf
		 */
		int evalMin(Board* board, Moves* moves,  char  depth, int alpha, int beta, int initialVal);

		/**
		* get all new moves from board and given moves
		* @param allNewMoves collection to be set with all new moves
		* @param moves  starting Moves
		* @param board starting Board
		* @param depth depth of all moves finding (aborted if > _level)
		* @param myColor find moves for Player with this color
		* @param oppColor opponent color
		*/
		void getAllNewMoves(vector<pair<Move*, pair<Board*, Moves*> > >& allNewMoves,
							Moves * moves,Board * board, char  depth,
							char myColor,char oppColor);

		/**
		 * delete moves from colletion except goodMove
		 * @param moves collection to be deleted
		 * @param goodMove move not to delete
		 */
		void deleteMoves(Moves* moves,Move* goodMove=NULL);

		/**
		 * set weight in game stage for player
		 * @param stage stage in game
		 * @param newWeights new weights of parameters
		 */
		void setWeights(unsigned char  stage, const vector<unsigned short> & newWeights)
		{
			for (unsigned char i=0; i<PARAMS_NUM; i++)
				weights[stage][i] = newWeights[i];
		}

		/**
		 * set weight in game stage for player
		 * @param stage stage in game
		 * @param newWeights new weights of parameters
		 */
		void setWeights(int stage, unsigned short *newWeights)
		{
			for (int i=0; i<PARAMS_NUM; i++)
				weights[stage][i] = newWeights[i];
		}

		

public:
		/**
		 * construct Plater with color and level
		 * @param color Player's color
		 * @param level Player's level
		 */
		Player(char color, char level = 0) :_color(color), _level(level)
		{
			_oppColor = (_color == COLOR_WHITE)? COLOR_BLACK : COLOR_WHITE;
			weights = new unsigned short *[STAGES];
			// init weights to maximal
			for (char i=0; i<STAGES; i++)
			{
				weights[i] = new unsigned short[PARAMS_NUM];
				for (char j=0; j<PARAMS_NUM; j++)
					weights[i][j] = numeric_limits<unsigned short>::max();
			}
		}



		/**
		 * destruct player
		 */
		virtual ~Player()
		{
			delete [] weights;
		}

		/**
		 * eval a current Game's board
		 * @return evaluation number
		 */
		int evalBoard();

		/**
		 * play turn
		 * @param maxVal to be set with maximum value
		 * @return choosen Move
		 */
		Move* play(int* maxVal=0);

		/**
		 * play turn with book
		 * @return choosen Move
		 */
		Move* playByBook(int* maxVal=0);

		/**
		 * @param level level of player (depth of min-max tree)
		 */
		void setLevel(char level)
		{
			_level = level;
		}

		char getLevel()
		{
			return _level;
		}

		/**
		 * learn how to play using Q-Learning
		 * @param initialBoard starting board
		 * @param myMove performing move
		 * @param expectedVal expected value for parameter
		 * @param oppMove opponent's move
		 * @param alpha Q-Learning alpha parameter
		 */
		void learn(Board* initialBoard, Move* myMove, int expectedVal, Move* oppMove, double alpha);

		/**
		 * @return weights used by player
		 */
		unsigned short ** getWeights()
		{
			return weights;
		}


		/**
		 * set weight  for player
		 * @param newWeights new weights of parameters
		 */
		void setWeights(const vector< vector<unsigned short> >& newWeights)
		{
			for (unsigned char i=0; i<STAGES; i++)
			{
				setWeights(i, newWeights[i]);
			}
		}

		/**
		 * set weight  for player
		 * @param newWeights new weights of parameters
		 */
		void setWeights(unsigned short **newWeights)
		{
			for (int i=0; i<STAGES; i++)
			{
				setWeights(i, newWeights[i]);
			}
		}


};

/**
 * moves comparator
 * compare according to number of moves possible for opponent after my move
 */
struct LessOpponentMoves {
	/**
	 * @param first left hand side
	 * @param second right hand side
	 * return true iff first < second (in terms of number of available moves)
	 */
	bool operator()(const pair<Move*, pair<Board*, Moves*> >  &first,
					const pair<Move*, pair<Board*, Moves*> >  &second)
	{
		return(first.second.second->size() < second.second.second->size());
	}
};





#endif
