package sCVR.preprocess.bean;

import sCVR.preprocess.nlp.WordScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YelpReview {
	String review_id;//
	String user_id;//
	String business_id;//
	int star;
	String date;
	String text;
	List<String> textList; //
	int useful;
	int funny;
	int cool;
	String type;
	String concept;//
	int sentiment; //
	List<String[]> wordScores; //
	int rate;//

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public List<String[]> getWordScores() {
		return wordScores;
	}

	public void setWordScores(List<String[]> wordScores) {
		this.wordScores = wordScores;
	}

	public String getConcept() {return concept;}
	public void setConcept(String concept) {this.concept = concept;}
	public int getSentiment() {return sentiment;}
	public void setSentiment(int sentiment) {this.sentiment = sentiment;}
	public String getReview_id() {
		return review_id;
	}
	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getBusiness_id() {
		return business_id;
	}
	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) throws IOException {
		this.text = text;
		textList = new ArrayList<String>(Arrays.asList(text.replaceAll("[^a-zA-Z' ]", " ").toLowerCase().split("\\s+")));
		System.out.println("WordScore start");
		wordScores = WordScore.scoreWrapper(text);
		System.out.println("WordScore end");
	}
	public int getUseful() {
		return useful;
	}
	public void setUseful(int useful) {
		this.useful = useful;
	}
	public int getFunny() {
		return funny;
	}
	public void setFunny(int funny) {
		this.funny = funny;
	}
	public int getCool() {
		return cool;
	}
	public void setCool(int cool) {
		this.cool = cool;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getTextList() {
		return textList;
	}
	public void setTextList(List<String> textList) {
		this.textList = textList;
	}
	
}
