package com.mrkaspa.tweetstream

/**
  * Created by michelperez on 2/2/16.
  */

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import twitter4j._
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

import scala.collection._

final case class Author(handle: String)

final case class Hashtag(name: String)

case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
}

final object EmptyTweet extends Tweet(Author(""), 0L, "")

object CretentialsUtils {
  val config = ConfigFactory.load()
  val appKey: String = config.getString("appKey")
  val appSecret: String = config.getString("appSecret")
  val accessToken: String = config.getString("accessToken")
  val accessTokenSecret: String = config.getString("accessTokenSecret")
}

class TwitterStreamClient(val actorSystem: ActorSystem) {
  val twitterStream = new TwitterStreamFactory().getInstance()

  def init = {
    twitterStream.setOAuthConsumer(CretentialsUtils.appKey, CretentialsUtils.appSecret)
    twitterStream.setOAuthAccessToken(new AccessToken(CretentialsUtils.accessToken, CretentialsUtils.accessTokenSecret))
    twitterStream.addListener(simpleUserListener)
    twitterStream.user()
  }

  def simpleUserListener = new UserStreamListener {
    def onStatus(s: Status) {
      actorSystem.eventStream.publish(Tweet(Author(s.getUser.getScreenName), s.getCreatedAt.getTime, s.getText))
    }

    override def onFriendList(friendIds: Array[Long]) = {}

    override def onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList) = {}

    override def onBlock(source: User, blockedUser: User) = {}

    override def onUserListSubscription(subscriber: User, listOwner: User, list: UserList) = {}

    override def onFollow(source: User, followedUser: User) = {}

    override def onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList) = {}

    override def onDirectMessage(directMessage: DirectMessage) = {}

    override def onUserListUpdate(listOwner: User, list: UserList) = {}

    override def onUnblock(source: User, unblockedUser: User) = {}

    override def onUnfollow(source: User, unfollowedUser: User) = {}

    override def onUserProfileUpdate(updatedUser: User) = {}

    override def onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList) = {}

    override def onDeletionNotice(directMessageId: Long, userId: Long) = {}

    override def onFavorite(source: User, target: User, favoritedStatus: Status) = {}

    override def onUnfavorite(source: User, target: User, unfavoritedStatus: Status) = {}

    override def onUserListDeletion(listOwner: User, list: UserList) = {}

    override def onUserListCreation(listOwner: User, list: UserList) = {}

    override def onStallWarning(warning: StallWarning) = {}

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) = {}

    override def onScrubGeo(userId: Long, upToStatusId: Long) = {}

    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int) = {}

    override def onException(ex: Exception) = {}
  }

  def stop = {
    twitterStream.cleanUp
    twitterStream.shutdown
  }

}
