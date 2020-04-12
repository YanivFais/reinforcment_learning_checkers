/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/
#pragma warning ( disable : 4786 )

#include <float.h>
#include <utility>
#include <algorithm>

#include "Game.h"
#include "Player.h"
#include "Board.h"
#include "Book.h"
#include "GameBoard.h"


int Player::evalBoard()
{
	return evalBoard(Game::getInstance()->getCurrentBoard());
}

int Player::evalBoard(Board* board)
{
	// using values as temp
	int values[PARAMS_NUM];
	return evalBoard(board, values);
}

 int Player::evalBoard(Board* board,  int* values)
{
	int stage = GameBoard::getStage(board);
	return GameBoard::evalBoard(board,_color,weights[stage], values);
}

void Player::getAllNewMoves(vector<pair<Move*, pair<Board*, Moves*> > >& allNewMoves,
							Moves* moves,
							Board * board, char depth,char myColor,char oppColor)
{
	// go over all moves , make them and find new moves from them, add to collection
	Moves::iterator it = moves->begin();
	for (unsigned char k=0; it != moves->end(); k++, it++)
	{
		Move* move = *it;
		Board* b1;
		Moves* moves1 = 0;
		b1 = GameBoard::makeMove(board,move,myColor);
		if (depth < _level)
			moves1 = GameBoard::findLegalMoves(b1,oppColor);
		pair<Board*, Moves*> p = make_pair(b1, moves1);
		allNewMoves[k] = make_pair(move, p);
	}
}

/**
 * The implemented MiniMax algorithm uses Alpha-Beta pruning in order to
 * decrease the number of evaluations and the number of tree nodes to be checked.
 * The use of alpha-beta pruning contributes a significant improvement to the
 * algorithm performance.
 * At each Minimax node, the available moves are sorted, so that the moves that result
 * in less opponent moves are checked first.
 * Using this method improves the performance,
 * since larger parts of the tree are eliminated due to Alpha-Beta pruning.
 */

int Player::evalMin(Board* board, Moves* moves, char  depth,  int alpha,  int beta,  int initialVal)
{
	int sz = moves->size();
	if (!sz)
	{ // no moves possible, this is worse..
		delete moves;
		return INT_MAX - depth;
	}
	int min = INT_MAX;
	Game::getInstance()->pushToHistory(board); // add for draw checking
	Moves::iterator it = moves->begin();
	// get all down-leaves possible from this leaf
	vector< pair< Move*, pair<Board*, Moves*> > > allNewMoves(moves->size());
	getAllNewMoves(allNewMoves,moves,board,-1,_oppColor,_color);
	// sort the moves (leaves) by opponent moves size
	if (depth < _level)
		std::sort(allNewMoves.begin(), allNewMoves.end(), LessOpponentMoves());
	vector<pair<Move*, pair<Board*, Moves*> > >::iterator it1 = allNewMoves.begin();
	bool prune = false;
	// go over leaves in sorted order
	for (; it1 != allNewMoves.end(); it1++)
	{
		Move* move = it1->first;
		Board* b1 = it1->second.first;
		Moves* nextMoves = it1->second.second;
		if (!prune)
		{
			//check draw situation
			bool draw = Game::getInstance()->checkDraw(b1);

			 int val;

			if (draw) // if draw then give positve value if we're losing, and negative if winning..
				val = -initialVal/(depth*depth);
			else
			{
				val = evalMax(b1, nextMoves, depth+1, alpha, beta, initialVal);					
			}

			beta = (val < beta)? val:beta; // alpha-beta update
			min = (val < min)? val:min;
			if (alpha >= beta) // done..
				prune = true;
		}
		else if (nextMoves)
			deleteMoves(nextMoves);
		delete b1;
		delete move;
	}
	delete moves;
	Game::getInstance()->popFromHistory(); // remove form history stack
	return min;
}

int Player::evalMax(Board* board, Moves* moves, char  depth,  int alpha,  int beta,  int initialVal)
{
	int sz = moves->size();
	if (!sz)
	{ // no moves,sure lose, this is worse..
		delete moves;
		return INT_MIN + depth;
	}
	Game::getInstance()->pushToHistory(board); // add to history for draw check
	 int max = INT_MIN;
	 // get all down leaves from this node
	vector<pair<Move*, pair<Board*, Moves*> > > allNewMoves(moves->size());
	getAllNewMoves(allNewMoves,moves,board,depth,_color,_oppColor);
	// sort the moves (leaves) by opponent moves size
	if (depth < _level)
		std::sort(allNewMoves.begin(), allNewMoves.end(), LessOpponentMoves());
	vector<pair<Move*, pair<Board*, Moves*> > >::iterator it1 = allNewMoves.begin();
	bool prune = false;
	// move over leaves in sorted order
	for (; it1 != allNewMoves.end(); it1++)
	{
		Move* move = it1->first;
		Board* b1 = it1->second.first;
		Moves* nextMoves = it1->second.second;
		if (!prune)
		{
			int val;
			// check for draw possibility
			bool draw = Game::getInstance()->checkDraw(b1);

			if (draw) // if draw then give positive value if we're losing,
				val = -initialVal/(depth*depth);  //and negative if winning..
			else
			{
				if (depth < _level)
				{
					val = evalMin(b1, nextMoves, depth+1, alpha, beta, initialVal);
				}
				else
				{
					val = evalBoard(b1); // final leaf
				}
			}
			alpha = (alpha > val)? alpha:val; // update alpha-beta
			max = (val > max)? val:max;
			if (alpha >= beta) // done :)
				prune = true;
		}
		else if (nextMoves)
			deleteMoves(nextMoves);
		delete b1;
		delete move;
	}
	delete moves;
	Game::getInstance()->popFromHistory(); // remove from stack
	return max;
}



Move* Player::play( int* maxVal)
{
	int initialVal = evalBoard(); // as of current board
	int alpha = INT_MIN; // initial alpha-beta
	int beta = INT_MAX;

	Board* board = Game::getInstance()->getCurrentBoard();
	Move* maxMove = 0;

	 int max = INT_MIN;
	unsigned char depth = 1;
	Moves * moves = GameBoard::findLegalMoves(board,_color) ; // find leaves for this node
	int sz = moves->size();
	if (!sz)
	{
		delete moves;
		return 0; // no moves,lose..
	}

	if (_level == 0)
	{ // play random,can't think..
		int r = (int)((float)rand() * (sz - 1)) / RAND_MAX;
		Moves::iterator it = moves->begin();
		for (int i=0; i<r; it++, i++);
		maxMove = *(it);
	}
	else if (sz == 1 && !maxVal) // save time,only one move..
	{
		maxMove = moves->front();
	}
	else
	{
		maxMove = 0;
		vector<pair<Move*, pair<Board*, Moves*> > > allNewMoves(moves->size());
		// find leaves from this node
		getAllNewMoves(allNewMoves,moves,board,depth,_color,_oppColor);
		// sort leaves by opponent moves size
		if (depth < _level)
			std::sort(allNewMoves.begin(), allNewMoves.end(), LessOpponentMoves());
		vector<pair<Move*, pair<Board*, Moves*> > >::iterator it1 = allNewMoves.begin();
		bool prune = false;

		// go over moves in sorted order
		for (; it1 != allNewMoves.end(); it1++)
		{
			Move* move = it1->first;
			Board* b1 = it1->second.first;
			Moves* nextMoves = it1->second.second;
			if (!prune)
			{
				bool draw = Game::getInstance()->checkDraw(b1); // check for draw
				int val;
				if (draw)
					val = 0; // nothing to do
				else
				{
					if (depth < _level)
						val = evalMin(b1, nextMoves, depth+1, alpha, beta, initialVal);
					else
						val = evalBoard(b1) - initialVal;
				}
				// update max value found
				if (val >= max || !maxMove)
				{
					maxMove = move;
					max = val;
				}
				if (val == max)
				{
					int r = rand();
					if (r > (RAND_MAX / sz))
						maxMove = move;
				}
				// update alpha-beta
				alpha = (alpha > val)? alpha:val;
				if (alpha >= beta)
					prune = true; // done :)

			}
			else if (nextMoves)
				deleteMoves(nextMoves);

			delete b1;
		}
	}
	deleteMoves(moves,maxMove);
	if (maxVal)
		*maxVal = max;
	return maxMove;
}

// playing using book
Move* Player::playByBook( int* maxVal)
{
	
	Board* board = Game::getInstance()->getCurrentBoard();
	if (_level == 0 || GameBoard::getStage(board) != STAGES-1)
		return play(maxVal); // only for game start, no more resources..

	Book* book = Game::getInstance()->getBook();

	if (_level > book->getBookLevel())
	{
		 // Book's level is less than player's level
		return play(maxVal); 
	}

	// get move from book used..
	Move * bookMove = book->getBookMove(board, _color);

	//no book move, play regularly..
	if (!bookMove)
	{
		return play(maxVal);
	}
	return bookMove;
}

// helper to delete collection

void Player::deleteMoves(Moves* moves,Move* goodMove /*=NULL*/)
{
	for (Moves::iterator movesIter = moves->begin(); movesIter != moves->end(); movesIter++)
		if ((*movesIter) != goodMove)
			delete (*movesIter);
	delete moves;
}

// Q-Learning function
void Player::learn(Board* initialBoard, Move* myMove, int expectedVal, Move* oppMove, double alpha)
{
	int size = Game::getInstance()->getBoardSize();
	double gamma = 1.0f;
	Board* tmp = Game::getInstance()->getCurrentBoard();
	int stage = GameBoard::getStage(initialBoard);
	int oldValues[PARAMS_NUM];
	int initialVal = evalBoard(initialBoard, oldValues);
	// If the expected return (current Q) is win/lose, set it to the appropriate reward.
	// The abs value of loss reward is higher that victory reward.
	if (expectedVal > Game::getInstance()->scaleWeight(50.0)) expectedVal = Game::getInstance()->scaleWeight(10.0);
	if (expectedVal < Game::getInstance()->scaleWeight(-50.0)) expectedVal = Game::getInstance()->scaleWeight(-20.0);
	Board* b1 = GameBoard::makeMove(initialBoard,myMove,_color);
	double oldQ = Game::getInstance()->scaleWeight( expectedVal - initialVal );
	double nextQ = 0.0;
	double rt = 0.0;
	if (!oppMove) // opponent cant play, victory for player.
		rt = 10; 
	else
	{
		Board * b2 = GameBoard::makeMove(b1,oppMove,_oppColor); // find board after opponent move
		Game::getInstance()->setCurrentBoard(b2);
		int nextVal;
		Move* nextMove = play(&nextVal);
		if (!nextMove) // player cant move after opponent move, player lost
			rt = -20; 
		else
		{
			rt = 0; // no reward, calulate Q(s(t+1), a')
			Board* b3 = GameBoard::makeMove(b2,nextMove,_color);
			nextQ = Game::getInstance()->scaleWeight(evalBoard(b3) - evalBoard(b2));
			delete b3;
		}	
		delete b2;
		delete nextMove;
	}
	// modify Q using Q-Learning formula
	double newQ = oldQ * (1-alpha) + alpha * (rt + gamma * nextQ);

	// adjust weights accroding to modified Q value.
	double totVal = 0.0;
	for (int i=0; i<PARAMS_NUM; i++)
		totVal += (weights[stage][i]!=0) ? (oldValues[i] / weights[stage][i]) : 0;
	if (totVal)
	{
		double maxAbs = 0.0;
		// avoiding overflow
		double newWeights[PARAMS_NUM];
		for (i=0; i<PARAMS_NUM; i++)
		{
			newWeights[i] = (weights[stage][i]!=0) 
				? Game::getInstance()->scaleWeight(weights[stage][i]) + 
				((newQ - oldQ) * ((oldValues[i] / weights[stage][i]) / totVal)) 
			: 0;
			if (newWeights[i] >= 0)
			{
				if (newWeights[i] < WEIGHT_THRESHOLD_DBL)
				newWeights[i] = WEIGHT_THRESHOLD_DBL;
			}
			else
				newWeights[i] = WEIGHT_THRESHOLD_DBL;
			maxAbs = (newWeights[i] > maxAbs)? fabs(newWeights[i]) : maxAbs;
		}
		for (i=0; i<PARAMS_NUM; i++) 
		{
			weights[stage][i] = (maxAbs!=0) ? Game::getInstance()->scaleWeight(newWeights[i] / maxAbs) : 0;
			if (weights[stage][i]==0)
			weights[stage][i] = Game::getInstance()->scaleWeight(WEIGHT_THRESHOLD_DBL);
		}
	}
	delete b1;
	Game::getInstance()->setCurrentBoard(tmp);
}



