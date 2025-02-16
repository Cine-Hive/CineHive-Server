<template>
  <div class="review-page">
    <div class="movie-info">
      <img v-if="posterPath" :src="'https://image.tmdb.org/t/p/original' + posterPath" alt="포스터" class="poster-image" />
      <div class="movie-details">
        <h2>{{ title }}</h2>
        <p class="overview">{{ overview }}</p>
      </div>
    </div>

    <div class="review-section">
      <h3>작품 해석</h3>
      <div v-for="review in reviews" :key="review.id" class="review">
        <p class="review-text">{{ review.text }}</p>
        <p class="review-meta">작성자: {{ review.author }} | {{ review.date }}</p>
      </div>
    </div>

    <div class="review-form">
      <h3>감상평 작성</h3>
      <textarea v-model="newReview.text" placeholder="여러분의 감상평을 남겨주세요"></textarea>
      <button @click="submitReview">등록</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      id: this.$route.query.id || '',
      posterPath: this.$route.query.posterPath || '',
      title: this.$route.query.title || '',
      overview: this.$route.query.overview || '',
      reviews: [],
      newReview: {
        text: '',
        author: '사용자', // 추후 로그인 기능 추가 가능
        date: new Date().toISOString().split('T')[0]
      }
    };
  },
  created() {
    this.fetchReviews();
  },
  methods: {
    async fetchReviews() {
      try {
        const response = await axios.get(`http://localhost:8081/movies/${this.id}/reviews`);
        this.reviews = response.data;
      } catch (error) {
        console.error("감상평을 불러오는 중 오류 발생:", error);
      }
    },
    async submitReview() {
      if (!this.newReview.text.trim()) {
        alert("감상평을 입력해주세요!");
        return;
      }
      try {
        await axios.post(`http://localhost:8081/movies/${this.id}/reviews`, this.newReview);
        this.fetchReviews(); // 감상평 목록 갱신
        this.newReview.text = ''; // 입력 필드 초기화
      } catch (error) {
        console.error("감상평 등록 중 오류 발생:", error);
      }
    }
  }
};
</script>

<style scoped>
.review-page {
  color: white;
  background-color: black;
  padding: 40px;
  border-radius: 10px;
}

.movie-info {
  display: flex;
  align-items: flex-start;
}

.poster-image {
  width: 150px;
  height: auto;
  margin-right: 20px;
  border-radius: 8px;
}

.movie-details {
  max-width: 600px;
}

.review-section {
  margin-top: 30px;
}

.review {
  border-bottom: 1px solid #555;
  padding: 10px 0;
}

.review-text {
  font-size: 16px;
}

.review-meta {
  font-size: 12px;
  color: gray;
}

.review-form {
  margin-top: 20px;

}

textarea {
  width: 100%;
  height: 100px;
  padding: 10px;
  border-radius: 5px;
  background-color: black; /* ✅ 배경색 검정 */
  color: white; /* ✅ 입력한 글자 색상 흰색 */
}

button {
  margin-top: 10px;
  padding: 10px;
  background-color: #1a1a1a;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 5px;
}

button:hover {
  background-color: #555;
}
</style>
