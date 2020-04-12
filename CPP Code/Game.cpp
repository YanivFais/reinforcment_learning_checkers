/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#include "Game.h"
#include "Player.h"
#include "Book.h"

Game* Game::instance = 0;
bool Game::_onlineLearningActive = false;

Game::Game() : book(0), _lastBlackMove(0), _lastWhiteMove(0), _learning(false)
{
	white = new Player(COLOR_WHITE);
	black = new Player(COLOR_BLACK);
}


void Game::setBoardSize(int bits)
{
	_bits = bits;
	_allowOnlineLearning = true;
	delete book;
	if (_lastBlackMove)
	{
		delete _lastBlackMove;
		_lastBlackMove = 0;
	}
	if (_lastWhiteMove)
	{
		delete _lastWhiteMove;
		_lastWhiteMove = 0;
	}



	switch (bits)
	{
	case 32:
		book = new ConcreteBook<32>;
		break;
	case 18:
		book = new ConcreteBook<18>;
		break;
	case 50:
		book = new ConcreteBook<50>;
		break;
	}
	clearHistory();
}


Player* Game::getPlayer(int color)
{
	switch (color)
	{
		case COLOR_BLACK:
			return black;
		case COLOR_WHITE:
			return white;
		default:
			throw "Illegal Color";
	}
}


Game::~Game()
{
	delete white;
	delete black;
	delete book;
	delete _lastBlackMove;
	delete _lastWhiteMove;
}

void Game::unDo(unsigned size)
{
	_allowOnlineLearning = false;
	if (_boardsHistory.size() >= size)
		for (unsigned i=0 ; i<size ; i++,_boardsHistory.pop_front())
			_boardsFuture.push_front(_boardsHistory.front()); // for redo
	}

void Game::reDo(unsigned size)
{
	if (_boardsFuture.size() >= size)
		for (unsigned i=0 ; i<size ; i++,_boardsFuture.pop_front())
			_boardsHistory.push_front(_boardsFuture.front()); // insert for undo..
	}


void Game::clearHistory()
{
	while (_boardsHistory.size()) // clear all history
	{
		Board* b = _boardsHistory.front();
		_boardsHistory.pop_front();
		delete b;
	}
}


void Game::clearReDo() // clear re do once another play has been made
{
	while (_boardsFuture.size())
	{
		Board* b = _boardsFuture.front();
		_boardsFuture.pop_front();
		delete b;
	}
}


void Game::pushToHistory(Board * board) // insert to history for re-do / draw check
{
	_boardsHistory.push_front(board);
}


void Game::popFromHistory() // get from history
{
	Board * board = _boardsHistory.front();
	_boardsHistory.pop_front();
}


bool Game::checkDraw(Board* board) const
{
	if (_boardsHistory.size() > 5) // draw is 5 moves with no "real" change (3 boards alike..)
	{
		int count = 0;
		for (list<Board*>::const_iterator it = _boardsHistory.begin(); it!=_boardsHistory.end(); it++)
		{
			switch (Game::getInstance()->getBoardSize())
			{
				case 18:
				{
					ParamBoard<18> *ba = (ParamBoard<18>*)*it;
					ParamBoard<18> *bb = (ParamBoard<18>*)board;
					if (ba->equals(*bb))
						count++;
					break;
				}
				case 32:
				{
					ParamBoard<32> *ba = (ParamBoard<32>*)*it;
					ParamBoard<32> *bb = (ParamBoard<32>*)board;
					if (ba->equals(*bb))
						count++;
					break;
				}
				case 50:
				{
					ParamBoard<50> *ba = (ParamBoard<50>*)*it;
					ParamBoard<50> *bb = (ParamBoard<50>*)board;
					if (ba->equals(*bb))
						count++;
					break;
				}
			}
		}
		return (count >= 2);
	}
	return false;
}
