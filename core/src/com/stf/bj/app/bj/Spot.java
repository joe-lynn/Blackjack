package com.stf.bj.app.bj;

import java.util.ArrayList;
import java.util.List;

import com.stf.bj.app.players.Human;
import com.stf.bj.app.players.Player;
import com.stf.bj.app.sprites.HandSprite;
import com.stf.bj.app.sprites.SpotSprite;
import com.stf.bj.app.table.Card;

public class Spot {

	private final int index;
	private Player player;
	private Human human;
	private double wager = 0;
	private double playerChips = 0;
	private String chipDisplay = "";
	private final List<Hand> hands;
	private final SpotSprite sprite;
	private boolean inPlay = false;
	int activeHands = 0;
	private boolean bettingInsurance = false;

	public Spot(int index, int hands) {
		this.index = index;
		this.hands = new ArrayList<Hand>(hands);
		for (int hand = 0; hand < hands; hand++) {
			this.hands.add(new Hand(this));
		}
		
		//setupSprites
	
		List<HandSprite> handSprites = new ArrayList<HandSprite>();
		for (Hand h : this.hands) {
			handSprites.add(h.getSprite());
		}
		sprite = new SpotSprite(this, handSprites);
	}

	public int getIndex() {
		return index;
	}

	public void addPlayer(Player player) {
		this.player = player;
		if (player.getClass() == Human.class) {
			human = (Human) player;
		}
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isHuman() {
		return human != null;
	}

	public Human getHuman() {
		return human;
	}

	public double getWager() {
		return wager;
	}

	public void setWager(double wager) {
		this.wager = wager;
	}

	public double getPlayerChips() {
		return playerChips;
	}

	public void setPlayerChips(double playerChips) {
		this.playerChips = playerChips;
	}

	public String getChipDisplay() {
		return chipDisplay;
	}

	public boolean isReady() {
		if ((hands.get(0).getChipStacks() != 0) && (hands.get(0).getChipStacks() != 1)) {
			return false;
		}
		return otherHandsEmpty();
	}

	public boolean singleBetOut() {
		if (hands.get(0).getChipStacks() != 1)
			return false;
		return otherHandsEmpty();
	}

	private boolean otherHandsEmpty() {
		for (int hand = 1; hand < hands.size(); hand++) {
			if (hands.get(hand).getChipStacks() != 0)
				return false;
		}
		return true;
	}

	public void removeAllChips() {
		for (Hand h : hands) {
			addChips(h, -h.getChipStacks(), true);
		}
		sprite.resetHandAnchors();
	}

	public void addChips(int hand, double stacks, boolean fromPlayerChips) {
		addChips(hands.get(hand), stacks, fromPlayerChips);
	}

	private void addChips(Hand hand, double stacks, boolean fromPlayerChips) {
		double change = hand.addChips(stacks, wager);
		if (fromPlayerChips) {
			playerChips -= change;
			chipDisplay = String.valueOf(playerChips);
		}
	}

	public Hand getHand(int hand) {
		return hands.get(hand);
	}

	public void addSplit(int sourceHandIndex) {
		Hand sourceHand = hands.get(sourceHandIndex);
		for (int newHandIndex = sourceHandIndex + 1; newHandIndex < hands.size(); newHandIndex++) {
			Hand newHand = hands.get(newHandIndex);
			if (!newHand.hasCards()) {
				splitChips(sourceHand, newHand);
				splitCards(sourceHand, newHand);
				splitSprites(sourceHand, newHand);
				return;
			}
		}
		throw new IllegalStateException("Didn't have something to split into????");
	}

	private void splitSprites(Hand sourceHand, Hand newHand) {
		sourceHand.getSprite().split(newHand.getSprite());

	}

	private void splitCards(Hand sourceHand, Hand newHand) {
		sourceHand.getHand().split(newHand.getHand());
		newHand.setActualIndex(activeHands);
		activeHands++;
	}

	private void splitChips(Hand sourceHand, Hand newHand) {
		addChips(newHand, 1, true);
	}

	public SpotSprite getSprite() {
		return sprite;
	}

	public void addCard(Card card, int handIndex) {
		hands.get(handIndex).addCard(card);
	}

	public int getActiveHands() {
		return activeHands;
	}

	public boolean isInPlay() {
		return inPlay;
	}

	public void setInPlay(boolean inPlay) {
		this.inPlay = inPlay;
		if(inPlay==true) {
			hands.get(0).setCurrentPlayingHand(true);
		}else {
			for(Hand h : hands) {
				if(h.isTheCurrentPlayingHand()) {
					h.setCurrentPlayingHand(false);
				}
			}
		}
	}

	// TODO Not sure I like how money and sprite manager also handle this elsewhere
	public void addBust(int handIndex) {
		activeHands--;
		hands.get(handIndex).clearCards();
		for (int otherHandIndex = handIndex + 1; otherHandIndex < hands.size(); otherHandIndex++) {
			Hand otherHand = hands.get(otherHandIndex);
			if(otherHand.hasCards()) {
				otherHand.setActualIndex(otherHand.getActualIndex() - 1);
			}
		}
	}

	public void clearCards() {
		for (Hand h : hands) {
			h.clearCards();
		}
		activeHands = 1;
	}

	public void ontoNextHand() {
		boolean handFound = false;
		for(int handIndex = 0; handIndex<hands.size(); handIndex++) {
			Hand hand = hands.get(handIndex);
			if(hand.isTheCurrentPlayingHand()) {
				handFound = true;
				hand.setCurrentPlayingHand(false);
			}else if (handFound) {
				hand.setCurrentPlayingHand(true);
				return;
			}
		}
	}

	public boolean isBettingInsurance() {
		return bettingInsurance;
	}

	public void setBettingInsurance(boolean bettingInsurance) {
		this.bettingInsurance = bettingInsurance;
		if(bettingInsurance) {
			playerChips -= wager/2;
		}else {
			playerChips += wager/2;
		}

		chipDisplay = String.valueOf(playerChips);
	}

	public void payInsurance() {
		bettingInsurance = false;
		playerChips += wager*3/2;

		chipDisplay = String.valueOf(playerChips);
	}

	public void takeInsurance() {
		bettingInsurance = false;
	}

}
