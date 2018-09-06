package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck;

    private final int PILES_NUM = 7;

    private Pile stockPile;
    private Pile discardPile;
    private Pile activePile;
    private Card firstClickedTarget = null;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();
    private Card cardToUncover;

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        activePile = card.getContainingPile();
        if (isCardDraggable(card)){
            if (firstClickedTarget == card) {
                System.out.println("double click");
                for (Pile pile : foundationPiles) {
                    if (isMoveValid(card, pile)) {

                        card.moveToPile(pile);
                    }
                }
                if (activePile.getTopCard().isFaceDown()){
                    activePile.getTopCard().flip();
                }
                firstClickedTarget = null;
            }
    //        else {
            firstClickedTarget = card;
            System.out.println("one click");
            if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
                card.moveToPile(discardPile);
                card.flip();
                card.setMouseTransparent(false);
                System.out.println("Placed " + card + " to the waste.");
                //            }
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        if (stockPile.isEmpty()){
            refillStockFromDiscard();
        }
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();

        if (isCardDraggable(card)) {
            activePile = card.getContainingPile();
            if (isOnStock()) {
                return;
            }
            double offsetX = e.getSceneX() - dragStartX;
            double offsetY = e.getSceneY() - dragStartY;

            draggedCards.clear();
            boolean isCardOnTop = (card == activePile.getTopCard());
            if (isCardOnTop) {
                addToDraggedCards(offsetX, offsetY, card);
            } else {
                for (Card currentCard : activePile.getCards()) {
                    boolean rankIsLower = card.getRankNumber() >= currentCard.getRankNumber();
                    if (rankIsLower && isOnTableau() && !currentCard.isFaceDown()) {
                        addToDraggedCards(offsetX, offsetY, currentCard);

                    }
                }
            }

        }
    };

    private void addToDraggedCards (double offsetX, double offsetY, Card card){
        draggedCards.add(card);
        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    }

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (!draggedCards.isEmpty()) {
            Card card = (Card) e.getSource();
            Pile pile = getValidIntersectingPile(card);
            //TODO
            if (pile != null) {
                System.out.println(activePile.cards.size() > draggedCards.size());
//
                if (isOnTableau() && (activePile.cards.size() > draggedCards.size())) {
                    Card cardToUncover = activePile.cards.get(activePile.cards.size() - draggedCards.size() - 1);
                    if (cardToUncover.isFaceDown()) {
                        cardToUncover.flip();
                    }
                }
                handleValidMove(card, pile);
                } else {
                    draggedCards.forEach(c -> MouseUtil.slideBack(c));
                    draggedCards.clear();
                }
            }
    };

    private boolean isOnTableau(){
        return  activePile.getPileType().equals(Pile.PileType.TABLEAU);
    }

    private boolean isOnStock(){
        return  activePile.getPileType().equals(Pile.PileType.STOCK);
    }

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        setupCards();
        dealCards();
    }

    private void setupCards() {
        deck.forEach(card -> {
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {

        List<Card> cardsToInverse = discardPile.getCards();
        Collections.reverse(cardsToInverse);
        stockPile.clear();

        for (Card card : cardsToInverse) {

            card.flip();
            stockPile.addCard(card);

        }
        discardPile.clear();

        //TODO

        System.out.println("Stock refilled from discard pile.");
    }

    private boolean isCardDraggable(Card card) {

        boolean topStockCard = card == stockPile.getTopCard();
        boolean topDiscardCard = card == discardPile.getTopCard();
        boolean topCard = card == card.getContainingPile().getTopCard();
        boolean foundationCard = card.getContainingPile().getPileType().equals(Pile.PileType.FOUNDATION);
        boolean tableauCard = card.getContainingPile().getPileType().equals(Pile.PileType.TABLEAU);
        boolean uncoveredCard = !card.isFaceDown();
        System.out.println(topStockCard);
        System.out.println();
        System.out.println(tableauCard);
        System.out.println(uncoveredCard);
        return  tableauCard && uncoveredCard || foundationCard && topCard || topStockCard || topDiscardCard;
    }

    public boolean isMoveValid(Card card, Pile destPile) {

        if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (!destPile.isEmpty()){
                boolean rankOneHigher = (card.getRankNumber() == destPile.getTopCard().getRankNumber() - 1);
                boolean oppositeColour = (!card.getSuit().getColour().equals(destPile.getTopCard().getSuit().getColour()));
                return rankOneHigher && oppositeColour;
            } else {
                return canPutOnEmptyPlace(card, Ranks.KING, destPile);
            }
        }

        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            return canPutOnEmptyPlace(card, Ranks.ACE, destPile) || hasSameSuitAndIsHigher(card, destPile);
        }

        return false;
    }

    private boolean hasSameSuitAndIsHigher(Card card, Pile destPile) {
        return !destPile.isEmpty() && destPile.getTopCard().getSuit().equals(card.getSuit())
                && destPile.getTopCard().getRankNumber() + 1 == card.getRankNumber();
    }

    private boolean canPutOnEmptyPlace(Card card, Ranks cardRank, Pile destPile) {
        return destPile.isEmpty() && card.getRank().equals(cardRank);
    }

    private Pile getValidIntersectingPile(Card card) {
        Pile result = null;
        List<Pile> allPiles = new ArrayList<>(tableauPiles);
        allPiles.addAll(foundationPiles);

        for (Pile pile : allPiles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty()) {
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        } else {
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());

        }
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < PILES_NUM; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        for (int i = 0; i < PILES_NUM; i++) {
            for (int j = 0; j < i + 1; j++) {
                tableauPiles.get(i).addCard(deckIterator.next());
            }
            tableauPiles.get(i).getTopCard().flip();
        }
        deckIterator.forEachRemaining(card -> stockPile.addCard(card));

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}