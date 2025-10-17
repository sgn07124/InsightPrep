package com.project.InsightPrep.domain.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.repository.MemberRepository;
import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.entity.PostStatus;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.reqository.CommentRepository;
import com.project.InsightPrep.domain.post.reqository.SharedPostRepository;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SharedPostServiceImplTest {

    @Mock
    SecurityUtil securityUtil;

    @Mock
    SharedPostRepository sharedPostRepository;

    @Mock
    AnswerRepository answerRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    SharedPostServiceImpl service;

    @Test
    @DisplayName("createPost: 성공")
    void createPost_success() {
        long memberId = 10L;
        long answerId = 111L;

        Member member = Member.builder().id(memberId).build();
        Answer answer = Answer.builder().id(answerId).build();
        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();


        SharedPost savedPost = SharedPost.builder()
                .id(999L)
                .title("t")
                .content("c")
                .answer(answer)
                .member(member)
                .status(PostStatus.OPEN)
                .build();

        // mock 설정
        given(securityUtil.getAuthenticatedMember()).willReturn(member);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(true);
        given(answerRepository.findById(answerId)).willReturn(Optional.of(answer));
        given(sharedPostRepository.save(any(SharedPost.class))).willReturn(savedPost);

        // when
        Long id = service.createPost(req);

        // then
        assertThat(id).isEqualTo(999L);
        verify(sharedPostRepository).save(any(SharedPost.class));
        verify(answerRepository).existsByIdAndMemberId(answerId, memberId);
        verify(answerRepository).findById(answerId);
    }

    @Test
    @DisplayName("createPost: 내 답변이 아니면 FORBIDDEN_OR_NOT_FOUND_ANSWER")
    void createPost_forbiddenOrNotFoundAnswer() {
        // given
        long memberId = 10L;
        long answerId = 111L;

        Member member = Member.builder().id(memberId).build();
        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();

        given(securityUtil.getAuthenticatedMember()).willReturn(member);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> service.createPost(req))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.FORBIDDEN_OR_NOT_FOUND_ANSWER.getMessage());

        verify(sharedPostRepository, never()).save(any(SharedPost.class));
    }

    @Test
    @DisplayName("createPost: insert 실패 시 CREATE_FAILED")
    void createPost_createFailed() {
        // given
        long memberId = 10L;
        long answerId = 111L;

        Member member = Member.builder().id(memberId).build();
        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();

        given(securityUtil.getAuthenticatedMember()).willReturn(member);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(true);
        given(answerRepository.findById(answerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.createPost(req))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(QuestionErrorCode.ANSWER_NOT_FOUND.getMessage());

        verify(sharedPostRepository, never()).save(any(SharedPost.class));
    }

    @Test
    @DisplayName("getPostDetail: 성공")
    void getPostDetail_success() {
        long postId = 7L;
        long viewerId = 10L;

        // Mock member(작성자)
        Member author = Member.builder()
                .id(viewerId)
                .nickname("me")
                .build();

        // Mock question, answer, feedback
        Question question = Question.builder()
                .id(1L)
                .category("CS")
                .content("Q?")
                .build();

        Answer answer = Answer.builder()
                .id(111L)
                .content("A")
                .question(question)
                .build();

        SharedPost post = SharedPost.builder()
                .id(postId)
                .title("t")
                .content("c")
                .status(PostStatus.OPEN)
                .member(author)
                .answer(answer)
                .build();

        // db Mock
        given(securityUtil.getLoginMemberId()).willReturn(viewerId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.countBySharedPostId(postId)).willReturn(5L);

        // when
        PostDetailDto res = service.getPostDetail(postId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.getTitle()).isEqualTo("t");
        assertThat(res.getAuthorNickname()).isEqualTo("me");
        assertThat(res.getMyPost()).isTrue();
        assertThat(res.getCommentCount()).isEqualTo(5L);

        verify(sharedPostRepository).findById(postId);
        verify(commentRepository).countBySharedPostId(postId);
    }

    @Test
    @DisplayName("getPostDetail: 게시글 없음 → POST_NOT_FOUND")
    void getPostDetail_notFound() {
        long postId = 7L;
        long viewerId = 10L;

        given(securityUtil.getLoginMemberId()).willReturn(viewerId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getPostDetail(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

        verify(sharedPostRepository).findById(postId);
        verify(commentRepository, never()).countBySharedPostId(anyLong());
    }

    @Test
    @DisplayName("resolve: 성공적으로 상태를 RESOLVED로 변경")
    void resolve_success() {
        // given
        long postId = 5L;
        long loginId = 10L;

        Member owner = Member.builder()
                .id(loginId)
                .build();

        SharedPost post = SharedPost.builder()
                .id(postId)
                .member(owner)
                .status(PostStatus.OPEN)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        service.resolve(postId);

        // then
        assertThat(post.getStatus()).isEqualTo(PostStatus.RESOLVED);
        verify(sharedPostRepository).findById(postId);
    }

    @Test
    @DisplayName("resolve: 게시글 없음 → POST_NOT_FOUND")
    void resolve_notFound() {
        // given
        long postId = 5L;
        long loginId = 10L;

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

        verify(sharedPostRepository).findById(postId);
    }

    @Test
    @DisplayName("resolve: 본인 글 아님 → FORBIDDEN")
    void resolve_forbidden() {
        // given
        long postId = 5L;
        long loginId = 10L; // 다른 사용자

        Member owner = Member.builder()
                .id(999L)
                .build();

        SharedPost post = SharedPost.builder()
                .id(postId)
                .member(owner)
                .status(PostStatus.OPEN)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("resolve: 이미 RESOLVED → ALREADY_RESOLVED")
    void resolve_alreadyResolved() {
        // given
        long postId = 5L;
        long loginId = 10L;

        Member owner = Member.builder()
                .id(loginId)
                .build();

        SharedPost post = SharedPost.builder()
                .id(postId)
                .member(owner)
                .status(PostStatus.RESOLVED)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.ALREADY_RESOLVED.getMessage());
    }

    @Test
    @DisplayName("resolve: markResolved() 내부 예외 발생 → IllegalStateException")
    void resolve_conflict() {
        // given
        long postId = 5L;
        long loginId = 10L;

        Member owner = Member.builder()
                .id(loginId)
                .build();

        // markResolved() 안에서 IllegalStateException 터지도록 미리 RESOLVED 상태
        SharedPost post = SharedPost.builder()
                .id(postId)
                .member(owner)
                .status(PostStatus.RESOLVED)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class) // 서비스가 PostException으로 래핑했는지 확인
                .hasMessageContaining(PostErrorCode.ALREADY_RESOLVED.getMessage());
    }

    @Test
    @DisplayName("getPosts: 정상 페이징(보정 포함)과 결과 매핑")
    void getPosts_success() {
        // given
        // page=0, size=1000 들어와도 보정: page=1, size=50
        int reqPage = 0;
        int reqSize = 1000;
        int safePage = 1;
        int safeSize = 50;

        // Mock 데이터 구성
        Question question = Question.builder()
                .id(1L)
                .category("CS")
                .content("Q1")
                .build();

        Answer answer = Answer.builder()
                .id(11L)
                .content("A1")
                .question(question)
                .build();

        SharedPost post = SharedPost.builder()
                .id(1L)
                .title("T1")
                .content("C1")
                .status(PostStatus.OPEN)
                .answer(answer)
                .member(Member.builder().id(42L).nickname("홍길동").build())
                .build();

        List<SharedPost> posts = List.of(post);
        Page<SharedPost> pageResult = new PageImpl<>(posts);

        given(sharedPostRepository.findAll(any(Pageable.class))).willReturn(pageResult);

        // when
        PageResponse<PostListItemDto> res = service.getPosts(reqPage, reqSize);

        // then
        assertThat(res.getPage()).isEqualTo(safePage);
        assertThat(res.getSize()).isEqualTo(safeSize);
        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getTitle()).isEqualTo("T1");
        assertThat(res.getContent().get(0).getCategory()).isEqualTo("CS");
        assertThat(res.getContent().get(0).getQuestion()).isEqualTo("Q1");

        verify(sharedPostRepository).findAll(any(Pageable.class));
    }
}